import os
import random
import re
import subprocess
import time
import math
from integration import (
    left_rectangle_method,
    right_rectangle_method,
    middle_rectangle_method,
    trapezoid_method,
    simpson_method,
    integrate_with_runge,
)
from equations import FUNCTIONS
from rich.console import Console
from rich.table import Table
from rich.panel import Panel
from rich.prompt import Prompt
from rich.live import Live
from rich.spinner import Spinner
from rich.text import Text

console = Console()


def play_meow():
    try:
        sounds_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "sounds")
        meow_files = [f for f in os.listdir(sounds_dir) if f.endswith(".mp3")]

        if not meow_files:
            return

        meow_file = random.choice(meow_files)
        meow_path = os.path.join(sounds_dir, meow_file)
        subprocess.Popen(
            ["afplay", meow_path], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL
        )
    except Exception:
        pass


class IntegrationApp:
    def __init__(self):
        self.error_msg = None
        self.step = "func"
        self.running = True
        self.max_iterations = 100
        self.is_calculating = False

    def run(self):
        self.show_welcome_animation()

        while self.running:
            if self.step == "func":
                self.show_function_selection()
            elif self.step == "limits":
                self.get_integration_limits()
            elif self.step == "eps":
                self.get_epsilon()
            elif self.step == "method":
                self.select_method()
            elif self.step == "calc":
                self.calculate_integral()
            elif self.step == "result":
                self.show_result()

    def show_welcome_animation(self):
        frames = [
            "  /\\_/\\  \n ( o.o ) \n  > ^ <  ",
            "  /\\_/\\  \n ( .o. ) \n  > ^ <  ",
            "  /\\_/\\  \n ( o o ) \n  > ^ <  ",
            "  /\\_/\\  \n ( -.- ) \n  > ^ <  ",
        ]
        with Live(auto_refresh=False, console=console) as live:
            for i in range(6):
                live.update(
                    Panel(
                        Text(frames[i % 4], style="bold magenta"),
                        title="[bold blue] Интегральный мяукулятор v1.1 🐱",
                        expand=False,
                    ),
                    refresh=True,
                )
                time.sleep(0.3)
            time.sleep(1)

    def show_function_selection(self):
        console.clear()
        table = Table(title="🐱 Доступные мяу-ункции 🐱")
        table.add_column("№", style="cyan", justify="center")
        table.add_column("Формурррла", style="magenta")
        table.add_column("Область мяууделения", style="green")

        for key, (formula_str, _, domain) in FUNCTIONS.items():
            domain_str = domain if domain else "Вся кошачья ось"
            table.add_row(key, str(formula_str), domain_str)

        console.print(Panel(table, border_style="blue"))

        if self.error_msg:
            console.print(f"[bold red]{self.error_msg}[/bold red]")
            self.error_msg = None

        choices = list(FUNCTIONS.keys())
        while True:
            func_choice = Prompt.ask(f"[yellow]🐾 Выбери мяу-ункцию {choices}[/yellow]")

            play_meow()

            if func_choice in choices:
                break
            self.show_error_cat("ш-ш-ш нет такого варианта, бака! подумай еще раз! 😼")

        self.formula_str, self.func, self.domain = FUNCTIONS[func_choice]
        self.step = "limits"

    def get_integration_limits(self):
        console.clear()
        console.print(f"[bold green]Ты выбрал:[/bold green] {self.formula_str}")

        if self.domain:
            console.print(f"[bold blue]Область мяуделения:[/bold blue] {self.domain}")

        num_pattern = r"^[+-]?(\d+(\.\d*)?|\.\d+)([eE][+-]?\d+)?$"

        self.a = self.meme_input(
            "Введи нижний пердел (a)", num_pattern, allow_negative=True, allow_zero=True
        )
        self.b = self.meme_input(
            "Введи верхний пердел (b)",
            num_pattern,
            allow_negative=True,
            allow_zero=True,
        )

        if self.a == self.b:
            self.error_msg = "Интеграл по точке? Семпай ты серьезно? 🤡"
            self.step = "func"
            return

        if self.a > self.b:
            console.print(
                "[yellow]🔄 Семпай, ты любишь всё делать наоборот! Муррр, поменяю местами a и b[/yellow]"
            )
            self.a, self.b = self.b, self.a

        try:
            for x in [self.a, self.b, (self.a + self.b) / 2]:
                val = self.func(x)
                if math.isinf(val) or math.isnan(val):
                    raise ValueError(f"Функция не определена в точке x={x} :(((")
        except Exception as e:
            self.error_msg = f"😱 УЖАСССССС Проблема с функцией: {str(e)}"
            self.step = "func"
            return

        # self.check_convergence()
        self.step = "eps"

    # def check_convergence(self):
    #     status_spinner = Spinner(
    #         "dots", text="[bold cyan]Мяулизируем сходимость... 🐱🧮[/bold cyan]"
    #     )

    #     with Live(status_spinner, refresh_per_second=10, console=console) as live:
    #         try:
    #             if not self.domain or "всю кошачью ось" in self.domain.lower():
    #                 self.has_discontinuity = False
    #                 live.stop()
    #                 return True

    #             self.discontinuities = []

    #             try:
    #                 val_a = self.func(self.a)
    #                 if math.isinf(val_a) or math.isnan(val_a):
    #                     self.discontinuities.append((self.a, "a"))
    #             except Exception:
    #                 self.discontinuities.append((self.a, "a"))

    #             try:
    #                 val_b = self.func(self.b)
    #                 if math.isinf(val_b) or math.isnan(val_b):
    #                     self.discontinuities.append((self.b, "b"))
    #             except Exception:
    #                 self.discontinuities.append((self.b, "b"))

    #             steps = 20
    #             dx = (self.b - self.a) / steps
    #             for i in range(1, steps):
    #                 x = self.a + i * dx
    #                 try:
    #                     val = self.func(x)
    #                     if math.isinf(val) or math.isnan(val):
    #                         self.discontinuities.append((x, "inside"))
    #                 except Exception:
    #                     self.discontinuities.append((x, "inside"))

    #             if not self.discontinuities:
    #                 self.has_discontinuity = False
    #                 live.stop()
    #                 return True

    #             self.has_discontinuity = True
    #             self.discontinuity_points = []

    #             for x, loc in self.discontinuities:
    #                 if loc == "a":
    #                     x_test_start = x + 0.001
    #                     x_test_end = x + 0.01
    #                 elif loc == "b":
    #                     x_test_start = x - 0.01
    #                     x_test_end = x - 0.001
    #                 else:
    #                     x_test_start = x - 0.01
    #                     x_test_end = x + 0.01

    #                 num_points = 5
    #                 p_values = []

    #                 for i in range(num_points):
    #                     try:
    #                         if loc == "a":
    #                             x_test = (
    #                                 x_test_start
    #                                 + i * (x_test_end - x_test_start) / num_points
    #                             )
    #                             delta = x_test - x
    #                         elif loc == "b":
    #                             x_test = (
    #                                 x_test_end
    #                                 - i * (x_test_end - x_test_start) / num_points
    #                             )
    #                             delta = x - x_test
    #                         else:
    #                             if i % 2 == 0:
    #                                 x_test = x + (i // 2 + 1) * 0.001
    #                                 delta = x_test - x
    #                             else:
    #                                 x_test = x - (i // 2 + 1) * 0.001
    #                                 delta = x - x_test

    #                         if abs(delta) < 1e-10:
    #                             continue

    #                         val_test = abs(self.func(x_test))
    #                         if val_test == 0:
    #                             p_values.append(0)
    #                             continue

    #                         if val_test < 1e-10 or abs(delta) < 1e-10:
    #                             p_values.append(0)
    #                             continue

    #                         p = -math.log(val_test) / math.log(abs(delta))

    #                         if math.isnan(p) or math.isinf(p):
    #                             continue

    #                         p_values.append(p)

    #                     except Exception:
    #                         continue

    #                 if not p_values:
    #                     continue

    #                 avg_p = sum(p_values) / len(p_values)

    #                 if math.isinf(self.a) or math.isinf(self.b):
    #                     self.converges = True

    #                 if avg_p >= 1:
    #                     self.converges = False
    #                     live.stop()
    #                     console.print(
    #                         Panel(
    #                             f"[bold red] *грустный мяу* Интеграл не существует[/bold red]\n\n"
    #                             f"Оценка порядка особенности в точке {x:.5f}: p ≈ {avg_p:.2f}\n"
    #                             "  /\\_/\\  \n ( x.x ) \n  > ^ <  ",
    #                             border_style="red",
    #                         )
    #                     )
    #                     time.sleep(2)
    #                     return False

    #             self.converges = True
    #             self.discontinuity_points = [x for x, _ in self.discontinuities]
    #             live.stop()
    #             return True

    #         except Exception as e:
    #             live.stop()
    #             console.print(
    #                 f"[red]Ошибка при проверке сходимости x_x: {str(e)}[/red]"
    #             )
    #             self.converges = None
    #             return None

    def get_epsilon(self):
        console.clear()
        console.print(
            f"[bold green]Хи-хи, перделы: a={self.a}, b={self.b}[/bold green]"
        )
        num_pattern = r"^[+-]?(\d+(\.\d*)?|\.\d+)([eE][+-]?\d+)?$"

        self.eps = self.meme_input(
            "Семпай, введи точность (например, 0.0001)",
            num_pattern,
            allow_negative=False,
            allow_zero=False,
            max_value=1.0,
        )

        if self.eps > 0.1:
            console.print("[yellow]📉 Выбрана довольно низкая точность [/yellow]")

        self.step = "method"

    def select_method(self):
        table = Table(title="🧮 Методы мяу-тегрирования 🧮")
        table.add_column("№", style="cyan", justify="center")
        table.add_column("Название", style="magenta")
        table.add_column("Порядок точности", style="green")

        methods = [
            ("1", "Левые мяу-угольники", "O(h)"),
            ("2", "Правые мяу-угольники", "O(h)"),
            ("3", "Средние мяу-угольники", "O(h²)"),
            ("4", "Мяу-пеции", "O(h²)"),
            ("5", "КотоСимпсон", "O(h⁴)"),
        ]

        for num, name, order in methods:
            table.add_row(num, name, order)

        console.print(Panel(table, border_style="blue"))

        while True:
            method_choice = Prompt.ask(
                "[yellow]🔢 Выбери мяу-тод [1, 2, 3, 4, 5][/yellow]"
            )
            play_meow()

            if method_choice in ["1", "2", "3", "4", "5"]:
                break
            self.show_error_cat("ш-ш-ш нет такого варианта, бака! подумай еще раз! 😼")

        methods = {
            "1": left_rectangle_method,
            "2": right_rectangle_method,
            "3": middle_rectangle_method,
            "4": trapezoid_method,
            "5": simpson_method,
        }

        self.method_name = {
            "1": "Левые мяу-угольники",
            "2": "Правые мяу-угольники",
            "3": "Средние мяу-угольники",
            "4": "Мяупеции",
            "5": "КотоСимпсон",
        }[method_choice]

        self.selected_method = methods[method_choice]
        self.step = "calc"

    def calculate_integral(self):
        console.clear()

        if hasattr(self, "converges") and not self.converges:
            self.step = "func"
            return

        status_spinner = Spinner(
            "dots", text="[bold cyan] ВЖЖЖЖЖЖ Считаю интеграл... 🐱🧮[/bold cyan]"
        )

        with Live(status_spinner, refresh_per_second=10, console=console) as live:
            try:
                initial_n = 4
                result, n = integrate_with_runge(
                    self.selected_method,
                    self.func,
                    self.a,
                    self.b,
                    self.eps,
                    initial_n,
                    self.max_iterations,
                )

                if math.isinf(result) or math.isnan(result):
                    self.error_msg = "🔥 О НЕТ НЕТ НЕТ НЕТ, Кажется, интеграл расходится или имеет особенности!"
                    self.step = "func"
                    return

                self.result = result
                self.n = n

                time.sleep(1.5)

            except Exception as e:
                self.error_msg = f"🙀 Упс! Что-то пошло не так: {str(e)}"
                self.step = "func"
                return

            live.update(
                Spinner(
                    "dots", text="[bold green]Финальные расчеты... 🐱✨[/bold green]"
                )
            )
            time.sleep(1)
            live.stop()

        self.step = "result"

    def show_result(self):
        console.clear()

        play_meow()
        frames = [
            "  /\\_/\\  \n ( o.o ) \n  > ^ <  [bold cyan]Результат готов![/bold cyan]",
            "  /\\_/\\  \n ( .o. ) \n  > ^ <  [bold cyan]Результат готов![/bold cyan]",
            "  /\\_/\\  \n ( o.o ) \n  > ^ <  [bold cyan]Результат готов![/bold cyan]",
        ]

        with Live(auto_refresh=False, console=console) as live:
            for frame in frames:
                live.update(Text.from_markup(frame))
                time.sleep(0.3)

        play_meow()

        result_table = Table(show_header=False, border_style="green")
        result_table.add_row("🎉 Готово! 🎉", style="bold green")
        result_table.add_row(f"🔢 Интеграл: {self.result:.10g}")
        result_table.add_row(f"📐 Разбиений: {self.n}")
        result_table.add_row(f"🎯 Точность: {self.eps}")

        if hasattr(self, "n") and self.n >= self.max_iterations * 4:
            result_table.add_row(
                "[yellow]⚠️ Достигнуто максимальное число разбиений[/yellow]"
            )

        console.print(
            Panel(
                result_table, title="[bold magenta]Результат вычисления[/bold magenta]"
            )
        )

        cat_result = f"""
        /\\_/\\  
        ( o.o ) 
        > ^ <  Результат: {self.result:.5f}
        """
        console.print(Panel(cat_result, border_style="blue"))

        while True:
            again = Prompt.ask("[yellow]🔁 Ещё раз? [1, 0][/yellow]")

            play_meow()

            if again in ["0", "1"]:
                break
            self.show_error_cat("ш-ш-ш нет такого варианта, бака! подумай еще раз! 😼")

        if again == "0":
            self.show_goodbye_animation()
            self.running = False
        else:
            self.step = "func"

    def meme_input(self, prompt, pattern=None, **kwargs):
        while True:
            user_input = Prompt.ask(f"[yellow]🐾 {prompt}[/yellow]")

            play_meow()

            if not user_input.strip():
                self.show_error_cat("Пусто! Попробуй ещё раз, не ленись 😼")
                continue

            if pattern and not re.fullmatch(pattern, user_input.strip()):
                self.show_error_cat(
                    "🥲 Эмммм а ниче тот факто что это не похоже на число!"
                )
                continue

            try:
                value = float(user_input)
                if not kwargs.get("allow_negative") and value < 0:
                    self.show_error_cat("😾 Отрицательные значения не катят!")
                    continue

                if not kwargs.get("allow_zero") and value == 0:
                    self.show_error_cat("😹 Ноль? Серьёзно?")
                    continue

                if kwargs.get("float_only") and "." not in user_input:
                    self.show_error_cat("🧃 Дробные значения, пожалуйста!")
                    continue

                if kwargs.get("max_value") is not None and value > kwargs["max_value"]:
                    self.show_error_cat(
                        f"📏 Слишком большое значение! Максимум {kwargs['max_value']}"
                    )
                    continue

                if math.isinf(value) or math.isnan(value):
                    self.show_error_cat("🤨 Бесконечность или не-число?")
                    continue

                if abs(value) > 1e10:
                    console.print("[yellow]🤔 Очень большое число! Уверен?[/yellow]")

                return value

            except Exception:
                self.show_error_cat("🐾 Это не число! Попробуй снова")

    def show_error_cat(self, message):
        error_panel = Panel(
            f"[bold red]{message}[/bold red]\n\n  /\\_/\\  \n ( x.x ) \n  > ^ <  ",
            border_style="red",
        )
        console.print(error_panel)

    def show_goodbye_animation(self):
        frames = [
            "  /\\_/\\  \n ( -.- ) \n  > ^ <  ПОКА!",
            "  /\\_/\\  \n ( ••• ) \n  > ^ <  ПАКА!",
            "  /\\_/\\  \n ( °°° ) \n  > ^ <  ПОКА!",
            "  /\\_/\\  \n ( ___ ) \n  > ^ <  ПОКА!",
        ]
        with Live(auto_refresh=False, console=console) as live:
            for i in range(8):
                live.update(
                    Panel(
                        Text(frames[i % 4], style="bold magenta"),
                        title="[bold blue]До новых встреч! 🐱",
                        expand=False,
                    ),
                    refresh=True,
                )
                time.sleep(0.3)


def show_functions_table():
    table = Table(title="Мяу-ункции")
    table.add_column("№", style="cyan", justify="center")
    table.add_column("Формула", style="magenta")
    table.add_column("Область определения", style="green")
    for key, (formula_str, _, domain) in FUNCTIONS.items():
        domain_str = domain if domain else "Вся числовая ось"
        table.add_row(key, str(formula_str), domain_str)
    console.print(table)


def show_methods_table():
    table = Table(title="Методы")
    table.add_column("№", style="cyan", justify="center")
    table.add_column("Название", style="magenta")
    table.add_column("Порядок точности", style="green")
    methods = [
        ("1", "Левые Мяу-угольники", "O(h)"),
        ("2", "Правые Мяу-угольники", "O(h)"),
        ("3", "Средние Мяу-угольники", "O(h²)"),
        ("4", "Мяупеции", "O(h²)"),
        ("5", "КотоСимпсон", "O(h⁴)"),
    ]
    for num, name, order in methods:
        table.add_row(num, name, order)
    console.print(table)


def main():
    app = IntegrationApp()
    play_meow()
    app.run()


if __name__ == "__main__":
    main()
