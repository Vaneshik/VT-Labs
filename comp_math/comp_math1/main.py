import numpy as np
import logging
from typing import Tuple

logging.basicConfig(level=logging.INFO, format="%(levelname)s: %(message)s")
logger = logging.getLogger(__name__)


class MatrixValidationError(Exception):
    """Кастомное исключение"""
    pass


def is_integer(s: str) -> bool:
    try:
        int(s)
        return True
    except ValueError:
        return False


def is_float(s: str) -> bool:
    try:
        float(s)
        return True
    except ValueError:
        return False


def read_matrix_from_file(filename: str) -> Tuple[int, float, int, np.ndarray, np.ndarray]:
    """Читает матрицу и параметры из файла с валидацией числовых значений."""
    try:
        with open(filename, 'r') as f:
            meta_line = f.readline().strip().replace(",", ".")
            if not meta_line:
                raise MatrixValidationError("Файл пуст")
            
            meta_data = meta_line.split()
            if len(meta_data) < 2:
                raise MatrixValidationError(
                    "Первая строка должна содержать: размерность, точность [макс_итерации]"
                )
            

            if not is_integer(meta_data[0]):
                raise MatrixValidationError("Размерность матрицы должна быть целым числом, а не символами.")
            if not is_float(meta_data[1]):
                raise MatrixValidationError("Точность должна быть числом, а не символами.")
            if len(meta_data) > 2 and not is_integer(meta_data[2]):
                raise MatrixValidationError("Максимальное число итераций должно быть целым числом, а не символами.")

            try:
                n = int(meta_data[0])
                if n < 3 or n > 20:
                    raise ValueError("Рекомендуется вводить матрицы размером от 3 до 20")
                
                tol = float(meta_data[1])
                if tol < 0 or tol > 1:
                    raise ValueError("Точность должна быть положительной и не слишком большой")
                
                max_iter = int(meta_data[2]) if len(meta_data) > 2 else 1000
                if max_iter < 0 or max_iter > 1000:
                    raise ValueError("Число итераций должно быть положительным и не слишком большим")
                
            except ValueError as ve:
                raise MatrixValidationError(f"Некорректный формат метаданных: {ve}")

            matrix_data = []
            for line_num, line in enumerate(f, 1):
                clean_line = line.strip().replace(",", ".")
                if not clean_line:
                    continue
                tokens = clean_line.split()

                for token in tokens:
                    if not is_float(token):
                        raise MatrixValidationError(
                            f"В строке {line_num + 1} обнаружен недопустимый символ: '{token}'"
                        )
                try:
                    row = list(map(float, tokens))
                except ValueError:
                    raise MatrixValidationError(
                        f"Некорректные данные в строке {line_num + 1}"
                    )
                
                if len(row) != n + 1:
                    raise MatrixValidationError(
                        f"Строка {line_num + 1} имеет неверную длину. Ожидалось {n + 1} элементов, получено {len(row)}"
                    )
                matrix_data.append(row)

            if len(matrix_data) != n:
                raise MatrixValidationError(
                    f"Ожидалось {n} строк, получено {len(matrix_data)}"
                )
                
            A = np.array([row[:-1] for row in matrix_data], dtype=np.float64)
            b = np.array([row[-1] for row in matrix_data], dtype=np.float64)
            
            return n, tol, max_iter, A, b
    except FileNotFoundError:
        logger.error("Файл не найден")
        exit(1)
    except Exception as e:
        logger.error(f"Ошибка чтения файла: {e}")
        exit(1)


def read_matrix_from_console() -> Tuple[np.ndarray, np.ndarray]:
    """Читает матрицу с консоли с валидацией введенных значений."""
    try:
        n_str = input("Введите размерность матрицы: ").strip()
        if not is_integer(n_str):
            raise ValueError("Размерность матрицы должна быть целым числом, а не символами")
        n = int(n_str)
        
        if n < 3 or n > 20:
            raise ValueError("Рекомендуется вводить матрицы размером от 3 до 20")
        
        print(f"Введите {n} строк (каждая содержит {n} коэффициентов + 1 свободный член):")
        A, b = [], []
        
        for i in range(n):
            while True:
                row_input = input(f"Строка {i+1}: ").strip()
                tokens = row_input.split()
                if len(tokens) != n + 1:
                    print(f"Некорректный ввод: ожидалось {n + 1} значений, получено {len(tokens)}. Попробуйте снова.")
                    continue

                valid = True
                for token in tokens:
                    if not is_float(token):
                        print(f"Некорректный ввод: '{token}' не является числом. Попробуйте снова.")
                        valid = False
                        break
                if not valid:
                    continue
                try:
                    parts = list(map(float, tokens))
                    A.append(parts[:-1])
                    b.append(parts[-1])
                    break
                except ValueError as ve:
                    print(f"Некорректный ввод: {ve}. Попробуйте снова.")
        
        return np.array(A, dtype=np.float64), np.array(b, dtype=np.float64)
    
    except ValueError as ve:
        logger.error(f"Ошибка ввода: {ve}")
        exit(1)
    except Exception as e:
        logger.error(f"Ошибка чтения с консоли: {e}")
        exit(1)


def validate_matrix(A: np.ndarray):
    """Валидация матрицы."""
    if np.all(A == 0):
        raise MatrixValidationError("Матрица состоит только из нулей, решение невозможно.")


def gauss_seidel(
    A: np.ndarray,
    b: np.ndarray,
    tol: float = 1e-6,
    max_iter: int = 1000
) -> Tuple[np.ndarray, int, np.ndarray]:
    """
    Метод Гаусса-Зейделя.
    """
    try:
        validate_matrix(A)
        
        n = A.shape[0]
        x = np.zeros_like(b, dtype=np.float64)
        error_history = np.zeros((max_iter, n))
        
        for it in range(max_iter):
            x_old = x.copy()
            for i in range(n):
                x[i] = (b[i] - np.dot(A[i, :i], x[:i]) - np.dot(A[i, i+1:], x_old[i+1:])) / A[i, i]
            
            error_history[it] = np.abs(x - x_old)
            
            if np.linalg.norm(x - x_old, np.inf) < tol:
                return x, it + 1, error_history[:it+1]
    except Exception as e:
        logger.error(f"Ошибка выполнения: {e}")
        exit(1)
    
    logger.warning(f"Достигнуто максимальное число итераций ({max_iter})")
    
    return x, max_iter, error_history


def print_errors(error_history: np.ndarray, n: int):
    """Выводит погрешности."""
    print("\nДетальная история погрешностей:")
    for i in range(n):
        print(f"Компонента x[{i+1}]: {error_history[-1, i]}")


def main():
    try:
        source = input("Способ ввода данных файл[f] / консоль[k]: ").lower().strip()
        
        if source.startswith('f'):
            filename = input("Введите имя файла: ")
            n, tol, max_iter, A, b = read_matrix_from_file(filename)
            
        elif source.startswith('k'):
            A, b = read_matrix_from_console()
            n = A.shape[0]
            
            max_iter_str = input("Максимальное количество итераций: ").strip()
            if not is_integer(max_iter_str):
                raise ValueError("Максимальное количество итераций должно быть целым числом, а не символами")
            max_iter = int(max_iter_str)
            if max_iter < 0 or max_iter > 100000:
                raise ValueError("Число итераций должно быть положительным и не слишком большим")
            
            tol_str = input("Точность (например, 1e-6): ").strip()
            if not is_float(tol_str):
                raise ValueError("Точность должна быть числом, а не символами")
            tol = float(tol_str)
            if tol < 0 or tol > 1:
                raise ValueError("Точность должна быть положительной и не слишком большой")
        else:
            raise ValueError("Некорректный метод ввода")
            
        x, iterations, error_history = gauss_seidel(A, b, tol, max_iter)
        

        if np.any(np.isnan(x)) or np.any(np.isinf(x)):
            raise ValueError("Походу нет решений")
        
        
        print("\n=== Решение системы ===")
        for i, val in enumerate(x, 1):
            print(f"x[{i}] = {val:.8f}")
        
        print(f"\nИтераций выполнено: {iterations}")
        print(f"Норма матрицы (inf): {np.linalg.norm(A, ord=np.inf):.2e}")
        
        print_errors(error_history, A.shape[0])
        
    except MatrixValidationError as mve:
        logger.error(f"Ошибка валидации матрицы: {mve}")
        exit(1)
    except ValueError as ve:
        logger.error(f"Ошибка ввода: {ve}")
        exit(1)
    except Exception as e:
        logger.error(f"Ошибка выполнения: {e}")
        exit(1)

if __name__ == "__main__":
    main()