import math

def poly1(x):
    return 5.74*x**3 - 2.95*x**2 - 10.28*x - 3.23

def dpoly1(x):
    return 17.22*x**2 - 5.9*x - 10.28

def poly2(x):
    return x**3 - 6*x**2 + 11*x - 6

def dpoly2(x):
    return 3*x**2 - 12*x + 11

def trans1(x):
    return math.sin(x) - x/2

def dtrans1(x):
    return math.cos(x) - 0.5

def trans2(x):
    return math.exp(x) - 2

def dtrans2(x):
    return math.exp(x)

FUNCTIONS = {
    '1': ('5.74x^3 - 2.95x^2 - 10.28x - 3.23', poly1, dpoly1),
    '2': ('x^3 - 6x^2 + 11x - 6', poly2, dpoly2),
    '3': ('sin(x) - x/2', trans1, dtrans1),
    '4': ('e^x - 2', trans2, dtrans2),
}


def F_sys1(v):
    x, y = v
    return [x**2 + y**2 - 1, x - y]

def J_sys1(v):
    x, y = v
    return [[2*x, 2*y], [1, -1]]

def F_sys2(v):
    x, y = v
    return [math.exp(x) + y - 1, x + math.exp(y) - 1]

def J_sys2(v):
    x, y = v
    return [[math.exp(x), 1], [1, math.exp(y)]]

SYSTEMS = {
    '1': ('x^2 + y^2 = 1; x - y = 0', F_sys1, J_sys1),
    '2': ('e^x + y = 1; x + e^y = 1', F_sys2, J_sys2),
}
