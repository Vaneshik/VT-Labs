package utils;

import enums.Enviroment;
import enums.Status;
import interfaces.WeatherControl;

import java.util.Vector;

public class Weather {
    private Vector<WeatherControl> currentWeather = new Vector<>();

    public Vector<WeatherControl> getCurrentWeather() {
        return currentWeather;
    }

    public void setCurrentWeather(Vector<WeatherControl> currentWeather) {
        this.currentWeather = currentWeather;
    }

    public class Sun implements WeatherControl {
        private Status status = Status.DEFAULT;
        private Enviroment enviroment = Enviroment.DEFAULT;

        public void setStatus(Status status) {
            this.status = status;
        }

        public Status getStatus() {
            return status;
        }

        public void setEnviroment(Enviroment enviroment) {
            this.enviroment = enviroment;
        }

        public Enviroment getEnviroment() {
            return enviroment;
        }

        public void goDown() {
            setStatus(Status.SETTING);
            System.out.println(this + " садится, " + enviroment + ".");
        }

        public void goUp() {
            setStatus(Status.DEFAULT);
            System.out.println(this + " встает, " + enviroment + ".");
        }

        @Override
        public void start() {
            this.goUp();
            currentWeather.add(this);
        }

        @Override
        public void stop() {
            this.goDown();
            currentWeather.remove(this);
        }

        @Override
        public String toString() {
            return "Солнце";
        }
    }

    public class Rain implements WeatherControl {
        private Status status = Status.DEFAULT;

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        @Override
        public void start() {
            this.setStatus(Status.RAINING);
            currentWeather.add(this);
            System.out.println(this + " " + this.getStatus() + ".");
        }

        @Override
        public void stop() {
            this.setStatus(Status.DEFAULT);
            currentWeather.remove(this);
            System.out.println(this + " закончился.");
        }

        @Override
        public String toString() {
            return "Дождь";
        }
    }

    public class Storm implements WeatherControl {
        private Status status = Status.DEFAULT;

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        @Override
        public void start() {
            this.setStatus(Status.RAGING);
            currentWeather.add(this);
            System.out.println("Началась " + this + ".");
        }

        @Override
        public void stop() {
            this.setStatus(Status.DEFAULT);
            currentWeather.remove(this);
            System.out.println(this + " закончилась.");
        }

        @Override
        public String toString() {
            return "Буря";
        }
    }

    public class Thunder implements WeatherControl {
        private Status status = Status.DEFAULT;

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public void rumble() {
            System.out.println(this + " гремит.");
        }

        @Override
        public void start() {
            this.setStatus(Status.RUMBLING);
            currentWeather.add(this);
            this.rumble();
        }

        @Override
        public void stop() {
            this.setStatus(Status.DEFAULT);
            currentWeather.remove(this);
            System.out.println(this + " закончился.");
        }

        @Override
        public String toString() {
            return "Гром";
        }
    }

    public class Lightning implements WeatherControl {
        private Status status = Status.DEFAULT;

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public void strike() {
            System.out.println(this + " ударила.");
        }

        @Override
        public void start() {
            this.setStatus(Status.FLASHING);
            currentWeather.add(this);
            this.strike();
        }

        @Override
        public void stop() {
            this.setStatus(Status.DEFAULT);
            currentWeather.remove(this);
            System.out.println(this + " закончилась.");
        }

        @Override
        public String toString() {
            return "Молния";
        }
    }

    public class ThunderStorm implements WeatherControl {
        private final Rain rain = new Rain();
        private final Storm storm = new Storm();
        private final Thunder thunder = new Thunder();
        private final Lightning lightning = new Lightning();

        public Rain getRain() {
            return rain;
        }

        public Storm getStorm() {
            return storm;
        }

        public Thunder getThunder() {
            return thunder;
        }

        public Lightning getLightning() {
            return lightning;
        }

        @Override
        public void start() {
            currentWeather.add(this);
            System.out.println("Началась " + this + ".");
        }

        @Override
        public void stop() {
            currentWeather.remove(this);
            System.out.println(this + " закончилась.");
        }

        @Override
        public String toString() {
            return "Гроза";
        }
    }

    @Override
    public String toString() {
        return "Погода";
    }

    public void runCurrentWeather() {
        for (WeatherControl w : currentWeather) {
            w.start();
        }
    }

    public void stop() {
        for (WeatherControl w : new Vector<>(this.currentWeather)) {
            w.stop();
        }
    }
}
