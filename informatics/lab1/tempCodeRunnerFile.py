
    if (not (2 <= base <= 36) and base > 0) or (not (-2 >= base >= -36) and base < 0):
        raise ValueError
    if n.startswith("-"):
        raise ValueError