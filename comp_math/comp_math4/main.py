import numpy as np
import matplotlib.pyplot as plt
from scipy import optimize
import pandas as pd
import subprocess
import os
import asyncio
import threading
import requests
import tempfile
from colorama import Fore, Style, init
import aiohttp
from concurrent.futures import ThreadPoolExecutor
import random
import warnings

warnings.filterwarnings("ignore")
np.seterr(over='ignore')

init(autoreset=True)

OLLAMA_MODEL = "llama3.2"
OLLAMA_API_URL = "http://localhost:11434/api/generate"
OLLAMA_TEMP = 0.3
OLLAMA_REPEAT_PENALTY = 1.2

SESSION_HASH = "x7wy4lmkhoa"
HF_API_URL = "https://roktimsardar123-english-japanese-anime-tts.hf.space/api/predict"
HF_FILE_PREFIX = "https://roktimsardar123-english-japanese-anime-tts.hf.space/file="


response_cache = {}


KAWAII_ERRORS = [
    "B-baka! That's not even a number! Try again! ٩(๑`^´๑)۶",
    "Hmph! Wrong format! Do it properly! (╯°□°)╯︵ ┻━┻",
    "Ara ara~ That's not how you enter data... (￣ω￣;)",
    "N-nani?! That doesn't look right! ヽ(≧Д≦)ノ",
    "Ugh! Why can't you follow simple instructions?! ಠ_ಠ",
    "W-what are you trying to do?! That's wrong! (●o≧д≦)o",
    "S-stupid! That's not how it works! (╬ ಠ益ಠ)",
    "*stares at input* ...Are you serious right now? (－‸ლ)",
    "*pouts* You're doing this wrong on purpose, aren't you? (￣^￣)",
    "I-it's not like I want correct input or anything... b-but fix this! (⁄ ⁄>⁄ ▽ ⁄<⁄ ⁄)",
]


http_session = requests.Session()


class TsundereLabAssistant:
    def __init__(self):
        self.x_data = None
        self.y_data = None
        self.results = {}
        self.best_function = None
        self.best_rmse = float("inf")
        self.audio_playing = False

        print(
            f"{Fore.CYAN}ʕ•ᴥ•ʔ Initializing... Preparing tsundere responses...{Style.RESET_ALL}"
        )
        self._preload_common_responses()
        print(
            f"{Fore.GREEN}(◕‿◕) Ready to help with function approximation desu~!{Style.RESET_ALL}"
        )

    def _preload_common_responses(self):
        common_actions = [
            "welcome",
            "input_method",
            "manual_input",
            "file_input",
            "calculating",
            "results",
            "goodbye",
        ]

        def load_responses():
            for action in common_actions:
                if action not in response_cache:
                    system_prompt, prompt = self.generate_tsundere_prompt(action)
                    response = self._fast_llama_call(system_prompt, prompt)
                    response_cache[action] = response

        thread = threading.Thread(target=load_responses)
        thread.daemon = True
        thread.start()

    def _fast_llama_call(self, system_prompt, prompt):
        payload = {
            "model": OLLAMA_MODEL,
            "prompt": f"<s>[INST] <<SYS>>\n{system_prompt}\n<</SYS>>\n\n{prompt} [/INST]",
            "temperature": OLLAMA_TEMP,
            "repeat_penalty": OLLAMA_REPEAT_PENALTY,
            "stream": False,
        }

        try:
            response = http_session.post(OLLAMA_API_URL, json=payload)
            if response.status_code == 200:
                result = response.json()
                return result.get("response", "").strip()
            else:
                print(
                    f"{Fore.RED}Error with Ollama API: Status {response.status_code}{Style.RESET_ALL}"
                )
                return "H-hey! Something went wrong with my thinking. Don't blame me!"
        except Exception as e:
            self.kawaii_error()
            print(f"{Fore.RED}Error calling Ollama API: {e}{Style.RESET_ALL}")
            return "H-hey! Something went wrong. Not that I care or anything..."

    def play_audio(self, audio_path):
        try:
            self.audio_playing = True
            subprocess.run(["afplay", audio_path], check=True)
        except Exception as e:
            self.kawaii_error()
            print(f"{Fore.RED}Error playing audio: {e}{Style.RESET_ALL}")
        finally:
            self.audio_playing = False

            try:
                os.unlink(audio_path)
            except Exception:
                self.kawaii_error()
                pass

    def generate_tsundere_prompt(self, action):
        system_prompt = """
        You are a tsundere anime girl assistant helping with a computational mathematics lab.
        Respond in the style of a tsundere character who acts tough and dismissive but secretly cares.
        Use appropriate anime-style expressions, occasional Japanese phrases, and tsundere mannerisms.
        Keep responses very short (1-2 sentences max). Include some "b-baka" or similar phrases occasionally.
        The user is working on a function approximation lab, and you're helping them enter data.
        """

        prompts = {
            "welcome": "Create short welcome message as tsundere math lab assistant to teacher 'Dmitriq' (pronounce his name)",
            "input_method": "Briefly ask if user wants file or manual data input, tsundere style",
            "manual_input": "Briefly ask how many points (8-12) for function approximation, tsundere style",
            "enter_point": "Very briefly ask for coordinates of point {}, tsundere style",
            "file_input": "Briefly ask for filename to load data, tsundere style",
            "calculating": "Say you're calculating, keep it very short and tsundere",
            "results": "Announce you found the best function, keep it short and tsundere",
            "goodbye": "Say a very brief tsundere goodbye",
        }

        prompt = prompts.get(action, "Say something short in tsundere style about math")
        if "enter_point" in action:
            point_num = action.split("_")[-1]
            prompt = prompts["enter_point"].format(point_num)

        return system_prompt, prompt

    def get_tsundere_response(self, action):
        if action in response_cache:
            return response_cache[action]

        system_prompt, prompt = self.generate_tsundere_prompt(action)
        response = self._fast_llama_call(system_prompt, prompt)

        response_cache[action] = response
        return response

    async def _request_tts(self, text):
        payload = {
            "data": [text, "派蒙 Paimon (Genshin Impact)", "English", 0.74, False],
            "fn_index": 2,
            "session_hash": SESSION_HASH,
        }

        async with aiohttp.ClientSession() as session:
            async with session.post(
                HF_API_URL, json=payload, headers={"Content-Type": "application/json"}
            ) as response:
                if response.status == 200:
                    result = await response.json()

                    if (
                        "data" in result
                        and len(result["data"]) >= 2
                        and isinstance(result["data"][1], dict)
                    ):
                        file_info = result["data"][1]

                        if "name" in file_info and file_info.get("is_file", False):
                            audio_file_path = file_info["name"]
                            audio_url = f"{HF_FILE_PREFIX}{audio_file_path}"

                            async with session.get(audio_url) as audio_response:
                                if audio_response.status == 200:
                                    return await audio_response.read()
        return None

    def speak(self, action):
        text = self.get_tsundere_response(action)
        print(f"\n{Fore.MAGENTA}{text}{Style.RESET_ALL}")

        temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".wav")
        temp_filename = temp_file.name
        temp_file.close()

        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)
        try:
            audio_data = loop.run_until_complete(self._request_tts(text))
            loop.close()

            if audio_data:
                with open(temp_filename, "wb") as f:
                    f.write(audio_data)

                self.play_audio(temp_filename)
        except Exception as e:
            self.kawaii_error()
            print(f"{Fore.RED}TTS Error: {e}{Style.RESET_ALL}")
            
        return text

    def kawaii_error(self):
        error_msg = random.choice(KAWAII_ERRORS)
        print(f"{Fore.RED}{error_msg}{Style.RESET_ALL}")

    def input_data(self):
        choice = (
            input(f"\n{Fore.CYAN}Your choice (file/manual): {Style.RESET_ALL}")
            .strip()
            .lower()
        )

        if choice == "file":
            self.speak("file_input")
            filename = input(f"\n{Fore.CYAN}Enter filename: {Style.RESET_ALL}").strip()
            try:
                data = pd.read_csv(filename)
                self.x_data = data.iloc[:, 0].values
                self.y_data = data.iloc[:, 1].values
                print(
                    f"{Fore.GREEN}ヾ(＾∇＾) Loaded {len(self.x_data)} points from {filename}!{Style.RESET_ALL}"
                )
                if len(self.x_data) < 8 or len(self.x_data) > 12:
                    print(
                        f"{Fore.RED}Number of points must be between 8 and 12. Try again!{Style.RESET_ALL}"
                    )
                    return self.input_data()
            except Exception as e:
                self.kawaii_error()
                print(f"{Fore.RED}Error loading file: {e}{Style.RESET_ALL}")
                print(
                    f"{Fore.RED}Please try again with a valid CSV file.{Style.RESET_ALL}"
                )
                return self.input_data()
        elif choice == "manual":
            print(
                f"\n{Fore.CYAN}How many points do you want to enter (8-12)? {Style.RESET_ALL}"
            )
            try:
                n_points = int(input())
                if n_points < 8 or n_points > 12:
                    print(
                        f"{Fore.RED}Number must be between 8 and 12. Try again!{Style.RESET_ALL}"
                    )
                    return self.input_data()

                print(
                    f"\n{Fore.CYAN}Enter {n_points} points in format 'x,y' (one per line):{Style.RESET_ALL}"
                )
                x_data = []
                y_data = []

                for i in range(n_points):
                    while True:
                        try:
                            point = input(
                                f"{Fore.CYAN}Point {i+1}: {Style.RESET_ALL}"
                            ).strip()
                            values = point.split(",")
                            if len(values) != 2:
                                print(
                                    f"{Fore.RED}Invalid format. Use 'x,y' format.{Style.RESET_ALL}"
                                )
                                continue

                            x = float(values[0])
                            y = float(values[1])
                            x_data.append(x)
                            y_data.append(y)
                            break
                        except ValueError:
                            self.kawaii_error()
                            print(
                                f"{Fore.RED}Invalid numbers. Try again.{Style.RESET_ALL}"
                            )

                self.x_data = np.array(x_data)
                self.y_data = np.array(y_data)
                print(
                    f"{Fore.GREEN}(｡･ω･｡) All points received! Arigatou~{Style.RESET_ALL}"
                )

            except ValueError:
                self.kawaii_error()
                print(
                    f"{Fore.RED}That's not a valid number. Try again.{Style.RESET_ALL}"
                )
                return self.input_data()

        else:
            self.kawaii_error()
            print(f"{Fore.RED}Invalid choice. Please enter 'file' or 'manual'.{Style.RESET_ALL}")
            return self.input_data()
            
        return True

    def linear_func(self, x, a, b):
        return a * x + b

    def poly2_func(self, x, a, b, c):
        return a * x**2 + b * x + c

    def poly3_func(self, x, a, b, c, d):
        return a * x**3 + b * x**2 + c * x + d

    def exp_func(self, x, a, b):
        return a * np.exp(b * x)

    def log_func(self, x, a, b):
        return a + b * np.log(np.maximum(x, 1e-10))

    def power_func(self, x, a, b):
        return a * np.power(np.maximum(x, 1e-10), b)

    def calculate_rmse(self, y_true, y_pred):
        return np.sqrt(np.mean((y_true - y_pred) ** 2))

    def pearson_correlation(self, x, y):
        return np.corrcoef(x, y)[0, 1]
    
    def determination_coefficient(self, y_true, y_pred):
        ss_res = np.sum((y_true - y_pred) ** 2)
        ss_tot = np.sum((y_true - np.mean(y_true)) ** 2)
        return 1 - (ss_res / ss_tot)

    def fit_functions(self):
        self.speak("calculating")

        x = self.x_data
        y = self.y_data

        x_extended = np.linspace(
            min(x) - 0.2 * (max(x) - min(x)), max(x) + 0.2 * (max(x) - min(x)), 1000
        )

        with ThreadPoolExecutor(max_workers=6) as executor:
            future_linear = executor.submit(self._fit_linear, x, y, x_extended)
            future_poly2 = executor.submit(self._fit_poly2, x, y, x_extended)
            future_poly3 = executor.submit(self._fit_poly3, x, y, x_extended)
            future_exp = executor.submit(self._fit_exp, x, y, x_extended)
            future_log = executor.submit(self._fit_log, x, y, x_extended)
            future_power = executor.submit(self._fit_power, x, y, x_extended)

            for future in [
                future_linear,
                future_poly2,
                future_poly3,
                future_exp,
                future_log,
                future_power,
            ]:
                try:
                    result = future.result()
                    if result:
                        func_name, func_results = result
                        self.results[func_name] = func_results

                        if func_results["rmse"] < self.best_rmse:
                            self.best_rmse = func_results["rmse"]
                            self.best_function = func_name
                except Exception as e:
                    self.kawaii_error()
                    print(
                        f"{Fore.RED}Error in parallel function fitting: {e}{Style.RESET_ALL}"
                    )

        return True

    def _fit_linear(self, x, y, x_extended):
        try:
            params, _ = optimize.curve_fit(self.linear_func, x, y)
            y_pred = self.linear_func(x, *params)
            rmse = self.calculate_rmse(y, y_pred)
            pearson = self.pearson_correlation(x, y)
            r2 = self.determination_coefficient(y, y_pred)

            return "Linear", {
                "params": params,
                "formula": f"y = {params[0]:.6f}*x + {params[1]:.6f}",
                "rmse": rmse,
                "pearson": pearson,
                "r2": r2,
                "y_pred": y_pred,
                "y_extended": self.linear_func(x_extended, *params),
                "x_extended": x_extended,
            }
        except Exception as e:
            self.kawaii_error()
            print(f"{Fore.RED}Error fitting linear function: {e}{Style.RESET_ALL}")
            return None

    def _fit_poly2(self, x, y, x_extended):
        try:
            params, _ = optimize.curve_fit(self.poly2_func, x, y)
            y_pred = self.poly2_func(x, *params)
            rmse = self.calculate_rmse(y, y_pred)
            r2 = self.determination_coefficient(y, y_pred)

            return "Polynomial 2", {
                "params": params,
                "formula": f"y = {params[0]:.6f}*x² + {params[1]:.6f}*x + {params[2]:.6f}",
                "rmse": rmse,
                "r2": r2,
                "y_pred": y_pred,
                "y_extended": self.poly2_func(x_extended, *params),
                "x_extended": x_extended,
            }
        except Exception as e:
            self.kawaii_error()
            print(
                f"{Fore.RED}Error fitting polynomial 2 function: {e}{Style.RESET_ALL}"
            )
            return None

    def _fit_poly3(self, x, y, x_extended):
        try:
            params, _ = optimize.curve_fit(self.poly3_func, x, y)
            y_pred = self.poly3_func(x, *params)
            rmse = self.calculate_rmse(y, y_pred)
            r2 = self.determination_coefficient(y, y_pred)

            return "Polynomial 3", {
                "params": params,
                "formula": f"y = {params[0]:.6f}*x³ + {params[1]:.6f}*x² + {params[2]:.6f}*x + {params[3]:.6f}",
                "rmse": rmse,
                "r2": r2,
                "y_pred": y_pred,
                "y_extended": self.poly3_func(x_extended, *params),
                "x_extended": x_extended,
            }
        except Exception as e:
            self.kawaii_error()
            print(
                f"{Fore.RED}Error fitting polynomial 3 function: {e}{Style.RESET_ALL}"
            )
            return None

    def _fit_exp(self, x, y, x_extended):
        try:
            exp_mask = y > 0
            if np.sum(exp_mask) > 3:
                params, _ = optimize.curve_fit(
                    self.exp_func, x[exp_mask], y[exp_mask], maxfev=10000
                )
                y_pred = self.exp_func(x, *params)
                rmse = self.calculate_rmse(y, y_pred)
                r2 = self.determination_coefficient(y, y_pred)

                return "Exponential", {
                    "params": params,
                    "formula": f"y = {params[0]:.6f}*exp({params[1]:.6f}*x)",
                    "rmse": rmse,
                    "r2": r2,
                    "y_pred": y_pred,
                    "y_extended": self.exp_func(x_extended, *params),
                    "x_extended": x_extended,
                }
        except Exception as e:
            print(f"{Fore.RED}Error fitting exponential function: {e}{Style.RESET_ALL}")
        return None

    def _fit_log(self, x, y, x_extended):
        try:
            log_mask = x > 0
            if np.sum(log_mask) > 3:
                params, _ = optimize.curve_fit(
                    self.log_func, x[log_mask], y[log_mask], maxfev=10000
                )
                y_pred = self.log_func(x, *params)
                rmse = self.calculate_rmse(y, y_pred)
                r2 = self.determination_coefficient(y, y_pred)

                return "Logarithmic", {
                    "params": params,
                    "formula": f"y = {params[0]:.6f} + {params[1]:.6f}*ln(x)",
                    "rmse": rmse,
                    "r2": r2,
                    "y_pred": y_pred,
                    "y_extended": self.log_func(x_extended, *params),
                    "x_extended": x_extended,
                }
        except Exception as e:
            self.kawaii_error()
            print(f"{Fore.RED}Error fitting logarithmic function: {e}{Style.RESET_ALL}")
        return None

    def _fit_power(self, x, y, x_extended):
        try:
            power_mask = (x > 0) & (y > 0)
            if np.sum(power_mask) > 3:
                params, _ = optimize.curve_fit(
                    self.power_func, x[power_mask], y[power_mask], maxfev=10000
                )
                y_pred = self.power_func(x, *params)
                rmse = self.calculate_rmse(y, y_pred)
                r2 = self.determination_coefficient(y, y_pred)

                return "Power", {
                    "params": params,
                    "formula": f"y = {params[0]:.6f}*x^{params[1]:.6f}",
                    "rmse": rmse,
                    "r2": r2,
                    "y_pred": y_pred,
                    "y_extended": self.power_func(x_extended, *params),
                    "x_extended": x_extended,
                }
        except Exception as e:
            self.kawaii_error()
            print(f"{Fore.RED}Error fitting power function: {e}{Style.RESET_ALL}")
        return None

    def save_results(self, filename="approximation_results.txt"):
        with open(filename, "w") as f:
            f.write("FUNCTION APPROXIMATION RESULTS\n")
            f.write("=============================\n\n")

            f.write("Input Data:\n")
            for i in range(len(self.x_data)):
                f.write(f"Point {i+1}: x = {self.x_data[i]}, y = {self.y_data[i]}\n")
            f.write("\n")

            f.write("Approximation Results:\n")
            for func_name, result in self.results.items():
                f.write(f"\n{func_name} Function:\n")
                f.write(f"  Formula: {result['formula']}\n")
                f.write(f"  RMSE: {result['rmse']:.6f}\n")
                f.write(f"  R²: {result['r2']:.6f}\n")

                if func_name == "Linear":
                    f.write(f"  Pearson Correlation: {result['pearson']:.6f}\n")

                if result["r2"] > 0.9:
                    msg = "Very strong fit"
                elif result["r2"] > 0.7:
                    msg = "Strong fit"
                elif result["r2"] > 0.5:
                    msg = "Moderate fit"
                elif result["r2"] > 0.3:
                    msg = "Weak fit"
                else:
                    msg = "Very weak fit"

                f.write(f"  Interpretation: {msg}\n")

            f.write(f"\nBest Approximating Function: {self.best_function}\n")
            f.write(f"Best RMSE: {self.best_rmse:.6f}\n")

        print(f"{Fore.GREEN}Results saved to {filename}{Style.RESET_ALL}")
        return filename

    def plot_results(self):
        plt.figure(figsize=(15, 10))

        plt.scatter(self.x_data, self.y_data, color="black", label="Data Points")

        colors = ["red", "blue", "green", "orange", "purple", "brown"]
        color_idx = 0

        for func_name, result in self.results.items():
            color = colors[color_idx % len(colors)]
            if func_name == self.best_function:
                linewidth = 2.5
                linestyle = "-"
            else:
                linewidth = 1.5
                linestyle = "--"

            plt.plot(
                result["x_extended"],
                result["y_extended"],
                color=color,
                linestyle=linestyle,
                linewidth=linewidth,
                label=f"{func_name}: {result['formula']} (RMSE: {result['rmse']:.4f})",
            )

            color_idx += 1

        plt.title("Function Approximation Results")
        plt.xlabel("x")
        plt.ylabel("y")
        plt.grid(True, alpha=0.3)
        plt.legend()

        x_range = max(self.x_data) - min(self.x_data)
        y_range = max(self.y_data) - min(self.y_data)
        plt.xlim(min(self.x_data) - 0.1 * x_range, max(self.x_data) + 0.1 * x_range)
        plt.ylim(min(self.y_data) - 0.1 * y_range, max(self.y_data) + 0.1 * y_range)

        plt.savefig("approximation_plot.png")
        plt.close()

        return "approximation_plot.png"

    def display_results(self):

        self.speak("results")

        print(f"\n{Fore.YELLOW}APPROXIMATION RESULTS:{Style.RESET_ALL}")
        print(f"{Fore.YELLOW}====================={Style.RESET_ALL}\n")

        for func_name, result in self.results.items():
            print(f"\n{Fore.CYAN}{func_name} Function:{Style.RESET_ALL}")
            print(f"  {Fore.WHITE}Formula: {result['formula']}{Style.RESET_ALL}")
            print(f"  {Fore.WHITE}RMSE: {result['rmse']:.6f}{Style.RESET_ALL}")
            print(f"  {Fore.WHITE}R²: {result['r2']:.6f}{Style.RESET_ALL}")

            if func_name == "Linear":
                print(
                    f"  {Fore.WHITE}Pearson Correlation: {result['pearson']:.6f}{Style.RESET_ALL}"
                )

            if result["r2"] > 0.9:
                msg = f"{Fore.GREEN}Very strong fit{Style.RESET_ALL}"
            elif result["r2"] > 0.7:
                msg = f"{Fore.LIGHTGREEN_EX}Strong fit{Style.RESET_ALL}"
            elif result["r2"] > 0.5:
                msg = f"{Fore.YELLOW}Moderate fit{Style.RESET_ALL}"
            elif result["r2"] > 0.3:
                msg = f"{Fore.LIGHTYELLOW_EX}Weak fit{Style.RESET_ALL}"
            else:
                msg = f"{Fore.RED}Very weak fit{Style.RESET_ALL}"

            print(f"  Interpretation: {msg}")

        print(
            f"\n{Fore.GREEN}Best Approximating Function: {self.best_function}{Style.RESET_ALL}"
        )
        print(f"{Fore.GREEN}Best RMSE: {self.best_rmse:.6f}{Style.RESET_ALL}")

        results_file = self.save_results()

        plot_file = self.plot_results()
        print(f"\n{Fore.GREEN}Results saved to {results_file}{Style.RESET_ALL}")
        print(f"{Fore.GREEN}Plot saved to {plot_file}{Style.RESET_ALL}")

        try:
            plt.figure(figsize=(15, 10))
            img = plt.imread(plot_file)
            plt.imshow(img)
            plt.axis("off")
            plt.show()
        except Exception as e:
            self.kawaii_error()
            print(f"{Fore.RED}Could not display plot: {e}{Style.RESET_ALL}")
            print(
                f"{Fore.YELLOW}Plot image saved as 'approximation_plot.png'{Style.RESET_ALL}"
            )

        return True

    def run(self):
        self.speak("welcome")
        self.speak("input_method")
        try:
            if not self.input_data():
                return False

            if not self.fit_functions():
                return False

            if not self.display_results():
                return False

            self.speak("goodbye")

            return True

        except KeyboardInterrupt:
            self.kawaii_error()
            print(f"\n{Fore.RED}Process interrupted by user.{Style.RESET_ALL}")
            self.speak("goodbye")
            return False
        except Exception as e:
            self.kawaii_error()
            print(f"{Fore.RED}An error occurred: {e}{Style.RESET_ALL}")
            return False


if __name__ == "__main__":
    assistant = TsundereLabAssistant()
    assistant.run()
