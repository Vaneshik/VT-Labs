from string import digits, ascii_uppercase
from bisect import bisect


def gen_fib(n):
    x = [1, 2]
    for i in range(2, n):
        x.append(x[i-1]+x[i-2])
    return x


def gen_fact(n):
    x = [1, 2]
    for i in range(3, n):
        x.append(i * x[-1])
    return x


ALP = digits + ascii_uppercase
FIB = gen_fib(100)
FACT = gen_fact(20)

GOLDEN = (1 + 5 ** 0.5) / 2
PHI_POS = [(pow(GOLDEN, i), i) for i in range(49, -1, -1)]
PHI_NEG = [(pow(GOLDEN, i), i) for i in range(-1, -51, -1)]


def dec_encode(n: str, base: int) -> str:
    n = int(n)  
    res = ""
    while n != 0:
        remainder = n % base
        n //= base
        if (remainder < 0):
            remainder += ((-1) * base)
            n += 1
        res += ALP[remainder]
    return res[::-1]


def dec_decode(n: str, base: int) -> int:
    res = 0
    for i, elem in enumerate(n[::-1]):
        res += base ** i * ALP.index(elem)
    return res


def fib_encode(n: str) -> str:
    n = int(n)
    res = ['0' for _ in range(100)]
    while n > 0:
        ind = bisect(FIB, n)
        if n == FIB[ind]:
            res[ind] == '1'
            n = 0
        else:
            res[ind-1] = '1'
            n -= FIB[ind-1]
    res = res[::-1]
    return "".join(res[res.index('1'):])


def fib_decode(n: str) -> int:
    res = 0
    length = len(n)
    for i in range(length):
        if n[length-i-1] == "1":
            res += FIB[i]
    return res


def fact_encode(n: str) -> str:
    n = int(n)
    divider = 2
    res = []
    while n > 0:
        res.append(str(n % divider))
        n //= divider
        divider += 1
    return ",".join(res[::-1])


def fact_decode(n: str) -> int:
    res = 0
    tmp = list(map(int, n.split(",")))
    length = len(tmp)
    for i in range(length):
        res += FACT[i] * tmp[length-i-1]
    return res


def berg_encode(n: str) -> str:
    n = int(n)
    res1, res2 = ["0" for _ in range(50)], ["0" for _ in range(50)]
    for i in range(50):
        if (PHI_POS[i][0] > n):
            continue
        res1[i] = "1"
        n -= PHI_POS[i][0]
    for i in range(50):
        if (n - PHI_NEG[i][0] > -0.00000000001):
            res2[i] = "1"
            n -= PHI_NEG[i][0]
    f_part = "".join(res1[res1.index("1"):])
    s_part = "".join(res2[:50-res2[::-1].index("1")])
    return f_part + "." + s_part


def berg_decode(n: str) -> int:
    f_part, s_part = n.split(".")
    res = 0
    for i in range(len(f_part)):
        if f_part[i] == "1":
            res += pow(GOLDEN, len(f_part)-i-1)
    for i in range(len(s_part)):
        if s_part[i] == "1":
            res += pow(GOLDEN, -1-i)
    return round(res)


def sym_encode(n: str, base: int) -> str:
    n, base, res = int(n), int(base), []
    while (n != 0):
        rem = n % base
        n //= base
        if (rem > base//2):
            rem = rem - base
            n += 1
        res.append(str(rem) if rem >= 0 else "{^" + str(-rem) + "}")
    return "".join(res[::-1])


def sym_decode(n: str, base: int) -> int:
    res, ind, tmp = 0, 0, []
    while ind < len(n):
        if n[ind].isdigit():
            tmp.append(int(n[ind]))
        else:
            tmp.append(-int(n[ind+2]))
            ind += 3
        ind += 1
    for i, elem in enumerate(tmp[::-1]):
        res += elem * pow(base, i)
    return res


def float_encode(n: str, base: int) -> str:
    f_part, s_part = n.split(".")
    f_part, s_part = int(f_part), int(s_part) / (10 ** len(s_part))
    res = ""
    while f_part != 0:
        remainder = f_part % base
        f_part //= base
        res += ALP[remainder]

    if not res:
        res += "0"
    res = res[::-1] + "."

    for _ in range(10):
        s_part *= base
        pre_calc = int(s_part)
        res += str(pre_calc)
        s_part -= pre_calc
    return res


def float_decode(n: str, base: int) -> int:
    res = 0
    f_part, s_part = n.split(",")
    for i, elem in enumerate(f_part[::-1]):
        res += ALP.index(elem) * base ** i
    for i, elem in enumerate(s_part):
        res += ALP.index(elem) * base ** (-(i+1))
    return res


def decode(n, base) -> int:
    if type(base) == int:
        res = float_decode(n, base) if ("," in n) or (
            "." in n) else dec_decode(n, base)
    elif base.endswith("C"):
        res = sym_decode(n, int(base[:-1]))
    elif base == "Fib":
        res = fib_decode(n)
    elif base == "Berg":
        res = berg_decode(n)
    elif base == "Fact":
        res = fact_decode(n)
    else:
        return ValueError
    return res


def encode(n, base) -> int:
    if type(base) == int:
        res = float_encode(n, base) if ("," in n) or (
            "." in n) else dec_encode(n, base)
    elif base.endswith("C"):
        res = sym_encode(n, int(base[:-1]))
    elif base == "Fib":
        res = fib_encode(n)
    elif base == "Berg":
        res = berg_encode(n)
    elif base == "Fact":
        res = fact_encode(n)
    else:
        return ValueError
    return res


def convert(n, base1, base2):
    return encode(str(decode(n, base1)), base2)


if __name__ == "__main__":
    task = [
        ("25307", 10, 9),
        ("10053", 7, 10),
        ("28D10", 15, 5),
        ("52,16", 10, 2),
        ("3B,64", 16, 2),
        ("73,14", 8, 2),
        ("0,001001", 2, 16),
        ("0,011001", 2, 10),
        ("1F,1E", 16, 10),
        ("75", 10, "Fib"),
        ("33{^2}00", "7C", 10),
        ("10100010", "Fib", 10),
        ("1000001.000001", "Berg", 10),
    ]

for i, (n, base1, base2) in enumerate(task, 1):
    res = convert(n, base1, base2)
    print("{:>2}) {:<15} in {:>4} to {:<4} = {}".format(
        i, n, base1, base2, res))
