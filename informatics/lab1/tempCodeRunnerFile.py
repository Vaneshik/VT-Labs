2) in enumerate(task, 1):
    res = convert(n, base1, base2)
    print("{:>2}) {:<15} in {:>4} to {:<4} = {}".format(
    i, n, base1, base2