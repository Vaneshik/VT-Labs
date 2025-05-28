import math


def left_rectangle_method(f, a, b, n):
    h = (b - a) / n
    return h * sum(f(a + i * h) for i in range(n))


def right_rectangle_method(f, a, b, n):
    h = (b - a) / n
    return h * sum(f(a + (i + 1) * h) for i in range(n))


def middle_rectangle_method(f, a, b, n):
    h = (b - a) / n
    return h * sum(f(a + (i + 0.5) * h) for i in range(n))


def trapezoid_method(f, a, b, n):
    h = (b - a) / n
    return h * (0.5 * f(a) + sum(f(a + i * h) for i in range(1, n)) + 0.5 * f(b))


def simpson_method(f, a, b, n):
    if n % 2 != 0:
        n += 1
    h = (b - a) / n
    return (
        h
        / 3
        * (
            f(a)
            + 2 * sum(f(a + i * h) for i in range(2, n, 2))
            + 4 * sum(f(a + i * h) for i in range(1, n, 2))
            + f(b)
        )
    )


def simple_integrate(f, a, b, n):
    h = (b - a) / n
    result = 0

    for i in range(n):
        x1 = a + i * h
        x2 = a + (i + 1) * h
        try:
            y1 = f(x1)
            y2 = f(x2)
            if math.isinf(y1) or math.isnan(y1):
                y1 = 0

            if math.isinf(y2) or math.isnan(y2):
                y2 = 0

            result += 0.5 * h * (y1 + y2)
        except:
            continue

    return result


def get_method_order(method):
    if method in [left_rectangle_method, right_rectangle_method, trapezoid_method]:
        return 1
    elif method == middle_rectangle_method or method == simpson_method:
        return 2
    return 1


def integrate_with_runge(method, f, a, b, eps, initial_n=4, max_iterations=20):
    n = initial_n
    k = 2
    p = get_method_order(method)

    try:
        integral_n = method(f, a, b, n)
        if math.isinf(integral_n) or math.isnan(integral_n):
            return integral_n, n
        iterations = 0

        while iterations < max_iterations:
            try:
                integral_kn = method(f, a, b, k * n)
                if math.isinf(integral_kn) or math.isnan(integral_kn):
                    return integral_kn, k * n
                if k**p - 1 == 0:
                    p = 1
                error = abs(integral_kn - integral_n) / (k**p - 1)
                if error < eps:
                    return integral_kn, k * n
                n *= k
                integral_n = integral_kn
                iterations += 1

            except ZeroDivisionError:
                return float("inf"), n

            except Exception as e:
                raise Exception(f"Ошибка при вычислении: {str(e)}") from e

        return integral_n, n

    except Exception as e:
        try:
            return simple_integrate(f, a, b, 1000), 1000
        except:
            return float("inf"), 0
