from functools import reduce

BIT_NAMES = ["r1", "r2", "i1", "r3", "i2", "i3", "i4"]
inp = list(map(int, list(input("Введите переданное сообщение: "))))

get_syndrom = lambda arr: str(reduce(lambda x, y: x ^ y, (inp[i] for i in arr)))
error_ind = int(get_syndrom([3, 4, 5, 6]) +
                get_syndrom([1, 2, 5, 6]) +
                get_syndrom([0, 2, 4, 6]), 2) - 1
inp[error_ind] = int(not inp[error_ind])

if error_ind == -1:
    print("Ошибок нет!")
else:
    print("Ошибка в бите с индексом", error_ind)
    print("Бит отвечает за", BIT_NAMES[error_ind])
    print("Сообщение без ошибок:", "".join([str(inp[i]) for i in [2,4,5,6]]))
