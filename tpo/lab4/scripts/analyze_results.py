#!/usr/bin/env python3
from __future__ import annotations

import argparse
import csv
import json
import math
import statistics
from collections import Counter, defaultdict
from pathlib import Path
from urllib.parse import parse_qs, urlparse

ROOT = Path(__file__).resolve().parents[1]
LOAD_RESULTS = ROOT / "load" / "result"
STRESS_RESULTS = ROOT / "stress" / "result"
DOCS = ROOT / "docs"
GRAPHS = DOCS / "graphs"

LIMIT_MS = 620
REQ_PER_USER_PER_MIN = 40
CONFIG_PRICES = {1: 3000, 2: 3900, 3: 7700}


def read_csv(path: Path) -> list[dict[str, str]]:
    if not path.exists():
        return []
    with path.open("r", encoding="utf-8-sig", newline="") as f:
        return list(csv.DictReader(f))


def to_int(value: str | None, default: int = 0) -> int:
    try:
        return int(float(value or default))
    except ValueError:
        return default


def percentile(values: list[int], pct: float) -> float:
    if not values:
        return 0.0
    sorted_values = sorted(values)
    index = (len(sorted_values) - 1) * pct
    lower = math.floor(index)
    upper = math.ceil(index)
    if lower == upper:
        return float(sorted_values[int(index)])
    return sorted_values[lower] + (sorted_values[upper] - sorted_values[lower]) * (index - lower)


def extract_config(row: dict[str, str], fallback: int | None = None) -> int | None:
    url = row.get("URL", "")
    if url:
        query = parse_qs(urlparse(url).query)
        if "config" in query:
            return to_int(query["config"][0], fallback or 0)
    label = row.get("label", "")
    for config in (1, 2, 3):
        if f"config {config}" in label.lower() or f"#{config}" in label:
            return config
    return fallback


def summarize(rows: list[dict[str, str]], fallback_config: int | None = None) -> dict[str, object]:
    elapsed = [to_int(row.get("elapsed")) for row in rows]
    timestamps = [to_int(row.get("timeStamp")) for row in rows]
    response_codes = [row.get("responseCode", "") for row in rows]
    failures = [row for row in rows if str(row.get("success", "")).lower() != "true"]
    duration_failures = [
        row for row in failures
        if "too long" in row.get("failureMessage", "").lower()
        or "longer than" in row.get("failureMessage", "").lower()
        or "620" in row.get("failureMessage", "")
    ]

    duration_sec = 0.0
    if len(timestamps) >= 2:
        duration_sec = max(timestamps) / 1000.0 - min(timestamps) / 1000.0
    throughput_rpm = len(rows) / (duration_sec / 60.0) if duration_sec > 0 else 0.0

    config = fallback_config
    for row in rows:
        config = extract_config(row, fallback_config)
        if config:
            break

    code_counts = Counter(response_codes)
    return {
        "config": config,
        "price": CONFIG_PRICES.get(config or 0),
        "samples": len(rows),
        "duration_sec": round(duration_sec, 3),
        "throughput_rpm": round(throughput_rpm, 3),
        "avg_ms": round(statistics.mean(elapsed), 3) if elapsed else 0.0,
        "median_ms": round(statistics.median(elapsed), 3) if elapsed else 0.0,
        "p90_ms": round(percentile(elapsed, 0.90), 3),
        "p95_ms": round(percentile(elapsed, 0.95), 3),
        "p99_ms": round(percentile(elapsed, 0.99), 3),
        "max_ms": max(elapsed) if elapsed else 0,
        "min_ms": min(elapsed) if elapsed else 0,
        "error_rate": round(len(failures) / len(rows), 5) if rows else 0.0,
        "http_403": code_counts.get("403", 0),
        "http_503": code_counts.get("503", 0),
        "duration_assertion_failures": len(duration_failures),
        "response_codes": dict(sorted(code_counts.items())),
        "suitable": bool(
            rows
            and code_counts.get("503", 0) == 0
            and code_counts.get("403", 0) == 0
            and len(duration_failures) == 0
            and (max(elapsed) if elapsed else 0) <= LIMIT_MS
        ),
    }


def load_time_series(rows: list[dict[str, str]], bucket_sec: int = 10) -> list[tuple[float, float, float]]:
    if not rows:
        return []
    start = min(to_int(row.get("timeStamp")) for row in rows)
    buckets: dict[int, list[int]] = defaultdict(list)
    for row in rows:
        timestamp = to_int(row.get("timeStamp"))
        bucket = int(((timestamp - start) / 1000) // bucket_sec)
        buckets[bucket].append(to_int(row.get("elapsed")))
    points = []
    for bucket, values in sorted(buckets.items()):
        minute = (bucket * bucket_sec) / 60.0
        throughput_rpm = len(values) * (60.0 / bucket_sec)
        avg_ms = statistics.mean(values)
        points.append((minute, throughput_rpm, avg_ms))
    return points


def stress_points(rows: list[dict[str, str]], bucket_sec: int = 10) -> list[tuple[int, float, float, int]]:
    if not rows:
        return []

    start = min(to_int(row.get("timeStamp")) for row in rows)
    grouped: dict[int, list[dict[str, str]]] = defaultdict(list)
    for row in rows:
        timestamp = to_int(row.get("timeStamp"))
        bucket = int(((timestamp - start) / 1000) // bucket_sec)
        grouped[bucket].append(row)

    points = []
    for _, group in sorted(grouped.items()):
        elapsed = [to_int(row.get("elapsed")) for row in group]
        users_values = [max(to_int(row.get("allThreads")), to_int(row.get("grpThreads"))) for row in group]
        errors = sum(1 for row in group if str(row.get("success", "")).lower() != "true")
        users = statistics.mean(users_values) if users_values else 0
        load_rpm = users * REQ_PER_USER_PER_MIN
        points.append((round(load_rpm), statistics.mean(elapsed), percentile(elapsed, 0.95), errors))
    return points


def svg_line_chart(
    path: Path,
    title: str,
    series: dict[str, list[tuple[float, float]]],
    x_label: str,
    y_label: str,
    threshold_y: float | None = None,
) -> None:
    width, height = 920, 520
    left, right, top, bottom = 80, 30, 50, 70
    colors = ["#1f77b4", "#2ca02c", "#d62728", "#9467bd", "#ff7f0e"]
    all_points = [point for points in series.values() for point in points]
    if not all_points:
        path.write_text("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"920\" height=\"180\"><text x=\"20\" y=\"40\">No data</text></svg>", encoding="utf-8")
        return

    min_x = min(x for x, _ in all_points)
    max_x = max(x for x, _ in all_points)
    min_y = 0
    max_y = max(y for _, y in all_points)
    if threshold_y is not None:
        max_y = max(max_y, threshold_y)
    max_x = max_x if max_x > min_x else min_x + 1
    max_y = max_y if max_y > min_y else min_y + 1
    max_y *= 1.12

    plot_w = width - left - right
    plot_h = height - top - bottom

    def sx(x: float) -> float:
        return left + (x - min_x) / (max_x - min_x) * plot_w

    def sy(y: float) -> float:
        return top + plot_h - (y - min_y) / (max_y - min_y) * plot_h

    parts = [
        f"<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"{width}\" height=\"{height}\" viewBox=\"0 0 {width} {height}\">",
        "<rect width=\"100%\" height=\"100%\" fill=\"white\"/>",
        f"<text x=\"{width / 2}\" y=\"28\" text-anchor=\"middle\" font-family=\"Arial\" font-size=\"20\">{title}</text>",
        f"<line x1=\"{left}\" y1=\"{top}\" x2=\"{left}\" y2=\"{top + plot_h}\" stroke=\"#333\"/>",
        f"<line x1=\"{left}\" y1=\"{top + plot_h}\" x2=\"{left + plot_w}\" y2=\"{top + plot_h}\" stroke=\"#333\"/>",
        f"<text x=\"{width / 2}\" y=\"{height - 20}\" text-anchor=\"middle\" font-family=\"Arial\" font-size=\"14\">{x_label}</text>",
        f"<text x=\"20\" y=\"{height / 2}\" text-anchor=\"middle\" transform=\"rotate(-90 20 {height / 2})\" font-family=\"Arial\" font-size=\"14\">{y_label}</text>",
    ]

    for i in range(6):
        y_value = min_y + (max_y - min_y) * i / 5
        y = sy(y_value)
        parts.append(f"<line x1=\"{left}\" y1=\"{y:.2f}\" x2=\"{left + plot_w}\" y2=\"{y:.2f}\" stroke=\"#eee\"/>")
        parts.append(f"<text x=\"{left - 10}\" y=\"{y + 4:.2f}\" text-anchor=\"end\" font-family=\"Arial\" font-size=\"11\">{y_value:.0f}</text>")

    for i in range(6):
        x_value = min_x + (max_x - min_x) * i / 5
        x = sx(x_value)
        parts.append(f"<line x1=\"{x:.2f}\" y1=\"{top}\" x2=\"{x:.2f}\" y2=\"{top + plot_h}\" stroke=\"#f2f2f2\"/>")
        parts.append(f"<text x=\"{x:.2f}\" y=\"{top + plot_h + 18}\" text-anchor=\"middle\" font-family=\"Arial\" font-size=\"11\">{x_value:.0f}</text>")

    if threshold_y is not None:
        y = sy(threshold_y)
        parts.append(f"<line x1=\"{left}\" y1=\"{y:.2f}\" x2=\"{left + plot_w}\" y2=\"{y:.2f}\" stroke=\"#d62728\" stroke-dasharray=\"8 6\"/>")
        parts.append(f"<text x=\"{left + plot_w - 4}\" y=\"{y - 6:.2f}\" text-anchor=\"end\" font-family=\"Arial\" font-size=\"12\" fill=\"#d62728\">limit {threshold_y:.0f} ms</text>")

    legend_x = left + 12
    legend_y = top + 18
    for index, (name, points) in enumerate(series.items()):
        color = colors[index % len(colors)]
        d = " ".join(f"{'M' if i == 0 else 'L'} {sx(x):.2f} {sy(y):.2f}" for i, (x, y) in enumerate(points))
        parts.append(f"<path d=\"{d}\" fill=\"none\" stroke=\"{color}\" stroke-width=\"2.5\"/>")
        for x, y in points:
            parts.append(f"<circle cx=\"{sx(x):.2f}\" cy=\"{sy(y):.2f}\" r=\"3\" fill=\"{color}\"/>")
        parts.append(f"<rect x=\"{legend_x}\" y=\"{legend_y + index * 22 - 10}\" width=\"14\" height=\"4\" fill=\"{color}\"/>")
        parts.append(f"<text x=\"{legend_x + 22}\" y=\"{legend_y + index * 22 - 5}\" font-family=\"Arial\" font-size=\"12\">{name}</text>")

    parts.append("</svg>")
    path.write_text("\n".join(parts), encoding="utf-8")


def markdown_table(headers: list[str], rows: list[list[object]]) -> str:
    lines = [
        "| " + " | ".join(headers) + " |",
        "| " + " | ".join("---" for _ in headers) + " |",
    ]
    for row in rows:
        lines.append("| " + " | ".join(str(item) for item in row) + " |")
    return "\n".join(lines)


def choose_config(metrics: dict[int, dict[str, object]]) -> int | None:
    suitable = [config for config, item in metrics.items() if item.get("suitable")]
    return min(suitable, key=lambda config: CONFIG_PRICES[config]) if suitable else None


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--selected-config", type=int, choices=[1, 2, 3], default=None)
    args = parser.parse_args()

    DOCS.mkdir(exist_ok=True)
    GRAPHS.mkdir(parents=True, exist_ok=True)

    load_metrics: dict[int, dict[str, object]] = {}
    throughput_series: dict[str, list[tuple[float, float]]] = {}

    for config in (1, 2, 3):
        rows = read_csv(LOAD_RESULTS / f"config{config}.csv")
        load_metrics[config] = summarize(rows, fallback_config=config)
        series = [(minute, throughput) for minute, throughput, _ in load_time_series(rows)]
        if series:
            throughput_series[f"Config {config}"] = series

    selected_config = args.selected_config or choose_config(load_metrics)

    stress_rows = read_csv(STRESS_RESULTS / "results.csv")
    stress_summary = summarize(stress_rows, fallback_config=selected_config)
    stress_series_data = stress_points(stress_rows)
    stress_series = {
        "avg response": [(load_rpm, avg_ms) for load_rpm, avg_ms, _, _ in stress_series_data],
        "p95 response": [(load_rpm, p95_ms) for load_rpm, _, p95_ms, _ in stress_series_data],
    } if stress_series_data else {}

    first_failure = None
    for load_rpm, avg_ms, p95_ms, errors in stress_series_data:
        if avg_ms > LIMIT_MS or p95_ms > LIMIT_MS or errors > 0:
            first_failure = {
                "load_rpm": load_rpm,
                "avg_ms": round(avg_ms, 3),
                "p95_ms": round(p95_ms, 3),
                "errors": errors,
            }
            break

    svg_line_chart(
        GRAPHS / "load_throughput.svg",
        "Load test throughput",
        throughput_series,
        "time, min",
        "throughput, requests/min",
    )
    svg_line_chart(
        GRAPHS / "stress_response_time.svg",
        "Stress test response time",
        stress_series,
        "load, requests/min",
        "response time, ms",
        threshold_y=LIMIT_MS,
    )

    metrics = {
        "load": load_metrics,
        "selected_config": selected_config,
        "stress": stress_summary,
        "first_failure": first_failure,
    }
    (DOCS / "metrics.json").write_text(json.dumps(metrics, ensure_ascii=False, indent=2), encoding="utf-8")

    rows = []
    for config, item in load_metrics.items():
        rows.append([
            config,
            f"${item.get('price')}",
            item.get("samples"),
            item.get("throughput_rpm"),
            item.get("avg_ms"),
            item.get("p95_ms"),
            item.get("max_ms"),
            item.get("http_403"),
            item.get("http_503"),
            item.get("duration_assertion_failures"),
            "yes" if item.get("suitable") else "no",
        ])

    analysis = [
        "# Lab 4 Analysis",
        "",
        "## Load test summary",
        "",
        markdown_table(
            ["Config", "Price", "Samples", "Throughput rpm", "Avg ms", "P95 ms", "Max ms", "403", "503", "Duration failures", "Suitable"],
            rows,
        ),
        "",
        f"Selected config: {selected_config if selected_config else 'not determined'}",
        "",
        "![Load throughput](graphs/load_throughput.svg)",
        "",
        "## Stress test summary",
        "",
        f"Stress samples: {stress_summary.get('samples')}",
        f"Stress avg ms: {stress_summary.get('avg_ms')}",
        f"Stress p95 ms: {stress_summary.get('p95_ms')}",
        f"First failure point: {first_failure if first_failure else 'not detected'}",
        "",
        "![Stress response time](graphs/stress_response_time.svg)",
        "",
    ]
    (DOCS / "analysis.md").write_text("\n".join(analysis), encoding="utf-8")

    print(f"Wrote {DOCS / 'analysis.md'}")
    print(f"Wrote {DOCS / 'metrics.json'}")
    print(f"Wrote {GRAPHS / 'load_throughput.svg'}")
    print(f"Wrote {GRAPHS / 'stress_response_time.svg'}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
