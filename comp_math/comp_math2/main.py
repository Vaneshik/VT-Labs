import sys
import numpy as np
import matplotlib.pyplot as plt

from solvers import (
    BisectionSolver,
    SecantSolver,
    SimpleIterSolver,
    NewtonSystemSolver,
    SolverError,
)
from equations import FUNCTIONS, SYSTEMS
from io_utils import input_float, read_file, isolate_roots


def choose_mode():
    mode = input("Ввод: (1) консоль, (2) файл? ").strip()
    if mode not in ("1", "2"):
        raise SolverError("Неверный режим ввода: ожидается '1' или '2'")
    return mode


def setup_equation_console():
    print("\nДоступные функции:")
    for key, (desc, _, _) in FUNCTIONS.items():
        print(f"  {key}: {desc}")
    fid = input("ID функции: ").strip()
    if fid not in FUNCTIONS:
        raise SolverError(f"Функция с ID='{fid}' не найдена")

    name, f, df = FUNCTIONS[fid]

    print("\nМетоды для уравнения:")
    print("  1: бисекция")
    print("  2: секущие")
    print("  3: простая итерация")
    choice = input("Метод (1/2/3): ").strip()
    methods = {"1": "bisection", "2": "secant", "3": "iter"}
    if choice not in methods:
        raise SolverError(f"Метод с кодом '{choice}' не поддерживается")
    method = methods[choice]

    a = input_float("a: ")
    b = input_float("b: ")
    eps = input_float("eps: ")

    intervals = isolate_roots(f, a, b)
    if not intervals:
        raise SolverError("Корней на указанном отрезке не обнаружено")
    if len(intervals) > 1:
        raise SolverError("На отрезке несколько корней, уточните интервал")
    a0, b0 = intervals[0]

    x0 = a0 if abs(f(a0)) < abs(f(b0)) else b0

    return name, f, df, a, b, a0, b0, x0, eps, method


def setup_system_console():
    print("\nДоступные системы:")
    for key, (desc, _, _) in SYSTEMS.items():
        print(f"  {key}: {desc}")
    sid = input("ID системы: ").strip()
    if sid not in SYSTEMS:
        raise SolverError(f"Система с ID='{sid}' не найдена")

    name, F, J = SYSTEMS[sid]

    coords = list(map(float, input("x0 y0: ").split()))
    if len(coords) != 2:
        raise SolverError("Нужно ввести два числа для x0 y0")
    eps = input_float("eps: ")

    return name, F, J, coords, eps


def solve_equation(name, f, df, a, b, a0, b0, x0, eps, method):
    print(f"\n— Решение уравнения \"{name}\" методом {method}")
    if method == "bisection":
        root, iters = BisectionSolver(f, a0, b0, eps).solve()
    elif method == "secant":
        root, iters = SecantSolver(f, a0, b0, eps).solve()
    else:
        root, iters, q = SimpleIterSolver(f, df, a0, b0, eps).solve(x0)

    resid = f(root)
    if abs(resid) > eps:
        raise SolverError(f"|f(root)|={abs(resid):.2e} > eps")

    print(f"Корень: {root:.6f}")
    print(f"f(root): {resid:.2e}")
    print(f"Итераций: {iters}")
    if method == "iter":
        print(f"Константа сходимости q: {q:.3f}")

    xs = np.linspace(a, b, 500)
    ys = [f(x) for x in xs]
    plt.figure()
    plt.plot(xs, ys, label=name)
    plt.axhline(0, color="black")
    plt.legend()
    plt.show()


def solve_system(name, F, J, x0, eps):
    print(f"\n— Решение системы \"{name}\" методом Ньютона")
    sol, iters, errors = NewtonSystemSolver(F, J, x0, eps).solve()

    vals = F(sol)
    if max(abs(v) for v in vals) > eps:
        raise SolverError(f"max|F(sol)|={max(abs(v) for v in vals):.2e} > eps")

    print(f"Решение: x={sol[0]:.6f}, y={sol[1]:.6f}")
    print(f"Итераций: {iters}")
    print("Вектор погрешностей по итерациям:")
    for i, err in enumerate(errors, 1):
        print(f"  шаг {i}: {err}")


    xs = np.linspace(sol[0] - 1, sol[0] + 1, 400)
    ys = np.linspace(sol[1] - 1, sol[1] + 1, 400)
    X, Y = np.meshgrid(xs, ys)
    Z1 = np.vectorize(lambda x, y: F([x, y])[0])(X, Y)
    Z2 = np.vectorize(lambda x, y: F([x, y])[1])(X, Y)

    plt.figure()
    plt.contour(X, Y, Z1, levels=[0], colors="blue")
    plt.contour(X, Y, Z2, levels=[0], colors="red")
    plt.scatter(sol[0], sol[1], color="black")
    plt.title(name)
    plt.show()


def main():
    try:
        mode = choose_mode()

        if mode == "1":
            task_type = input("Задача: (1) уравнение, (2) система? ").strip()
            if task_type == "1":
                params = setup_equation_console()
                solve_equation(*params)
            elif task_type == "2":
                name, F, J, x0, eps = setup_system_console()
                solve_system(name, F, J, x0, eps)
            else:
                raise SolverError("Ожидается '1' или '2' для задачи.")
        else:
            path = input("Путь к файлу: ").strip()
            fid, a, b, eps, method, x0 = read_file(path)
            if fid in FUNCTIONS and method in ("bisection", "secant", "iter"):
                name, f, df = FUNCTIONS[fid]
                validate_interval = isolate_roots  # reuse
                intervals = validate_interval(f, a, b)
                if len(intervals) != 1:
                    raise SolverError("Файл задаёт 0 или >1 корней на отрезке.")
                a0, b0 = intervals[0]
                if x0 is None:
                    x0 = a0 if abs(f(a0)) < abs(f(b0)) else b0
                solve_equation(name, f, df, a, b, a0, b0, x0, eps, method)
            elif fid in SYSTEMS and method == "newton_sys":
                name, F, J = SYSTEMS[fid]
                if not x0 or len(x0) != 2:
                    raise SolverError("В файле нужно задать x0 y0 для системы.")
                solve_system(name, F, J, x0, eps)
            else:
                raise SolverError("Неподдерживаемый ID или метод в файле.")

    except SolverError as err:
        print(f"\nОшибка: {err}")
        sys.exit(1)
    except Exception as exc:
        print(f"\nНеожиданная ошибка: {exc}")
        sys.exit(2)


if __name__ == "__main__":
    main()