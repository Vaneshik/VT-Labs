import numpy as np
from solvers import SolverError

def input_float(prompt):
    try:
        return float(input(prompt))
    except ValueError:
        raise SolverError("Ожидалось число с плавающей точкой")

def read_file(path):
    try:
        with open(path) as f:
            lines = [ln.strip() for ln in f if ln.strip()]
        fid = lines[0]
        a, b, eps = map(float, lines[1].split())
        method = lines[2]
        x0 = None
        if len(lines) > 3:
            x0 = list(map(float, lines[3].split()))
        return fid, a, b, eps, method, x0
    except Exception as e:
        raise SolverError(f"Ошибка чтения файла: {e}")

def isolate_roots(f, a, b, n=1000):
    if a >= b:
        raise SolverError(f"Неверный диапазон: a={a} >= b={b}")
    xs = np.linspace(a, b, n+1)
    intervals = []
    for i in range(len(xs)-1):
        x0, x1 = xs[i], xs[i+1]
        f0, f1 = f(x0), f(x1)
        if f0 == 0:
            intervals.append((x0, x0))
        if f0 * f1 < 0:
            intervals.append((x0, x1))
    if f(xs[-1]) == 0:
        intervals.append((xs[-1], xs[-1]))
    return intervals
