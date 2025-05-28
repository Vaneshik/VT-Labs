import math
import numpy as np

class SolverError(Exception):
    pass

class BisectionSolver:
    def __init__(self, f, a, b, tol):
        if tol <= 0:
            raise SolverError("eps должен быть > 0")
        self.f, self.a, self.b, self.tol = f, a, b, tol

    def solve(self):
        fa, fb = self.f(self.a), self.f(self.b)
        if fa == 0:
            return self.a, 0
        if fb == 0:
            return self.b, 0
        if fa * fb > 0:
            raise SolverError("Нет смены знака на [a,b]")
        a, b, it = self.a, self.b, 0
        while (b - a) / 2 > self.tol:
            c = (a + b) / 2
            fc = self.f(c)
            it += 1
            if fc == 0:
                return c, it
            if fa * fc < 0:
                b, fb = c, fc
            else:
                a, fa = c, fc
        return (a + b) / 2, it

class SecantSolver:
    def __init__(self, f, a, b, tol):
        if tol <= 0:
            raise SolverError("eps должен быть > 0")
        self.f, self.x0, self.x1, self.tol = f, a, b, tol

    def solve(self):
        x0, x1 = self.x0, self.x1
        f0, f1 = self.f(x0), self.f(x1)
        if f0 == 0:
            return x0, 0
        if f1 == 0:
            return x1, 0
        it = 0
        while abs(x1 - x0) > self.tol:
            if f1 == f0:
                raise SolverError("Деление на ноль в методе секущих")
            x2 = x1 - f1 * (x1 - x0) / (f1 - f0)
            x0, x1 = x1, x2
            f0, f1 = f1, self.f(x1)
            it += 1
            if it > 10000:
                raise SolverError("Метод секущих не сошелся")
        return x1, it

class SimpleIterSolver:
    def __init__(self, f, df, a, b, tol):
        if tol <= 0:
            raise SolverError("eps должен быть > 0")
        if a >= b:
            raise SolverError("Неверный диапазон")
        self.f, self.df, self.a, self.b, self.tol = f, df, a, b, tol

    def compute_lambda(self):
        M = max(abs(self.df(self.a)), abs(self.df(self.b)))
        if M == 0:
            raise SolverError("Производная равна нулю на концах интервала")
        return -1 / M

    def verify_convergence(self, lam):
        xs = np.linspace(self.a, self.b, 200)
        q = max(abs(1 + lam * self.df(x)) for x in xs)
        if q >= 1:
            raise SolverError(f"Условие сходимости не выполнено: q={q:.3f} >= 1")
        return q

    def solve(self, x0):
        lam = self.compute_lambda()
        q = self.verify_convergence(lam)
        x, it = x0, 0
        while True:
            xn = x + lam * self.f(x)
            it += 1
            if abs(xn - x) < self.tol:
                return xn, it, q
            if it > 10000:
                raise SolverError("Метод простой итерации не сошелся за 10000 итераций")
            x = xn

class NewtonSystemSolver:
    def __init__(self, F, J, x0, tol, max_iter=100):
        if tol <= 0:
            raise SolverError("eps должен быть > 0")
        self.F, self.J, self.x0, self.tol, self.max_iter = F, J, x0, tol, max_iter

    def solve(self):
        x = np.array(self.x0, float)
        errors = []
        for k in range(1, self.max_iter + 1):
            Fx = np.array(self.F(x), float)
            Jx = np.array(self.J(x), float)
            try:
                dx = np.linalg.solve(Jx, -Fx)
            except np.linalg.LinAlgError:
                raise SolverError("Якобиан вырожден; решение невозможно")
            x_new = x + dx
            errors.append(np.abs(dx).tolist())
            if np.linalg.norm(dx, np.inf) < self.tol:
                return x_new.tolist(), k, errors
            x = x_new
        raise SolverError("Метод Ньютона для системы не сошелся за max_iter")
