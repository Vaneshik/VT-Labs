rror("Неподдерживаемый ID или метод в файле.")

    except SolverError as err:
        print(f"\nОшибка: {err}")
        sys.exit(1)
    except Exception as exc:
        print(f"\nНеожиданная ошибка: {exc}")
        sys.exit(2)