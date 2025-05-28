import math

FUNCTIONS = {
    "1": ("3x^3 - 4x^2 + 5x - 16", lambda x: 3*x**3 - 4*x**2 + 5*x - 16, None),
    "2": ("sin(x) + x^2", lambda x: math.sin(x) + x**2, None),
    "3": ("e^x - x", lambda x: math.exp(x) - x, None),
    "4": ("1 / (1 + x^2)", lambda x: 1 / (1 + x**2), None),
    "5": ("cos(x) * x", lambda x: math.cos(x) * x, None),
}