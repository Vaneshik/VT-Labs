import math


def count_divisors(n):
    if n <= 0:
        return 0

    count = 0
    sqrt_n = int(math.sqrt(n))

    for i in range(1, sqrt_n + 1):
        if n % i == 0:
            if i == n // i:
                count += 1
            else:
                count += 2

    return count


def triangle_number(n):
    return n * (n + 1) // 2


def highly_divisible_triangular(min_divisors):
    n = 1
    while True:
        tri_num = triangle_number(n)
        divisors = count_divisors(tri_num)
        if divisors > min_divisors:
            return tri_num
        n += 1


if __name__ == '__main__':
    print(f'Первое треугольное число с более чем 5 делителями: {highly_divisible_triangular(5)}')
    print(f'Первое треугольное число с более чем 500 делителями: {highly_divisible_triangular(500)}')
