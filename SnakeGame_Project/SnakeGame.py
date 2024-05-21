import random
from tkinter import *

GAME_WIDTH = 600
GAME_HEIGHT = 400
SPEED = 120
SPACE_SIZE = 50
LARGE_SPACE_SIZE = 70  # Size for large food
BODY_PARTS = 3
SNAKE_COLOR = "green"
FOOD_COLOR = "yellow"
LARGE_FOOD_COLOR = "red"
BACKGROUND_COLOR = "#000000"
HIGH_SCORE_FILE = "highscore.txt"  # File to store the high score

class Snake:
    def __init__(self):
        self.body_size = BODY_PARTS
        self.coordinates = []
        self.squares = []

        for i in range(0, BODY_PARTS):
            self.coordinates.append([0, 0])

        for x, y in self.coordinates:
            square = canvas.create_rectangle(x, y, x + SPACE_SIZE, y + SPACE_SIZE, fill=SNAKE_COLOR, tag="snake")
            self.squares.append(square)

class Food:
    def __init__(self, is_large=False):
        self.is_large = is_large
        if self.is_large:
            size = LARGE_SPACE_SIZE
        else:
            size = SPACE_SIZE
        x = random.randint(0, (GAME_WIDTH // size) - 1) * size
        y = random.randint(0, (GAME_HEIGHT // size) - 1) * size

        self.coordinates = [x, y]

        if self.is_large:
            canvas.create_oval(x, y, x + LARGE_SPACE_SIZE, y + LARGE_SPACE_SIZE, fill=LARGE_FOOD_COLOR, tag="large_food")
        else:
            canvas.create_oval(x, y, x + SPACE_SIZE, y + SPACE_SIZE, fill=FOOD_COLOR, tag="food")

def next_turn(snake, food):
    if not paused:
        x, y = snake.coordinates[0]

        if direction == "up":
            y -= SPACE_SIZE
        elif direction == "down":
            y += SPACE_SIZE
        elif direction == "left":
            x -= SPACE_SIZE
        elif direction == "right":
            x += SPACE_SIZE

        snake.coordinates.insert(0, (x, y))

        square = canvas.create_rectangle(x, y, x + SPACE_SIZE, y + SPACE_SIZE, fill=SNAKE_COLOR)
        snake.squares.insert(0, square)

        if food.is_large:
            food_size = LARGE_SPACE_SIZE
        else:
            food_size = SPACE_SIZE

        food_x, food_y = food.coordinates
        if x >= food_x and x < food_x + food_size and y >= food_y and y < food_y + food_size:
            global score, food_counter, SPEED
            if food.is_large:
                score += 3
                SPEED = max(SPEED - 10, 10)  # Decrease the delay by 10 units (increase speed), with a minimum delay of 10ms
                canvas.delete("large_food")
            else:
                score += 1
                canvas.delete("food")
            food_counter += 1
            label.config(text="Score:{}".format(score))

            if food_counter % 5 == 0:
                food = Food(is_large=True)
            else:
                food = Food()
        else:
            del snake.coordinates[-1]
            canvas.delete(snake.squares[-1])
            del snake.squares[-1]

        if check_collision(snake):
            game_over()
        else:
            window.after(SPEED, next_turn, snake, food)

def change_direction(new_direction):
    global direction

    if new_direction == 'left' and direction != 'right':
        direction = new_direction
    elif new_direction == 'right' and direction != 'left':
        direction = new_direction
    elif new_direction == 'up' and direction != 'down':
        direction = new_direction
    elif new_direction == 'down' and direction != 'up':
        direction = new_direction

def check_collision(snake):
    x, y = snake.coordinates[0]

    if x < 0 or x >= GAME_WIDTH or y < 0 or y >= GAME_HEIGHT:
        return True

    for body_part in snake.coordinates[1:]:
        if x == body_part[0] and y == body_part[1]:
            return True

    return False

def game_over():
    global score

    # Check if the current score is higher than the previous high score
    try:
        with open(HIGH_SCORE_FILE, "r") as file:
            high_score = int(file.read())
    except FileNotFoundError:
        high_score = 0

    if score > high_score:
        with open(HIGH_SCORE_FILE, "w") as file:
            file.write(str(score))
        high_score_label.config(text="High Score: {}".format(score))
    else:
        with open(HIGH_SCORE_FILE, "r") as file:
            high_score_label.config(text="High Score: {}".format(file.read()))

    canvas.delete(ALL)
    canvas.create_text(canvas.winfo_width() / 2, canvas.winfo_height() / 2, font=('consolas', 70), text="YOU LOSE", fill="red", tag="gameover")
    try_again_button.pack()

def start_game():
    global snake, food, direction, score, food_counter, paused, SPEED
    score = 0
    food_counter = 0
    direction = 'down'
    paused = False
    SPEED = 120  # Reset speed
    label.config(text="Score:{}".format(score))
    canvas.delete(ALL)
    snake = Snake()
    food = Food()
    next_turn(snake, food)

def toggle_pause():
    global paused
    paused = not paused
    if not paused:
        next_turn(snake, food)

window = Tk()
window.title("Snake Game")
window.resizable(False, False)

score = 0
direction = 'down'
food_counter = 0
paused = False

label = Label(window, text="Score:{}".format(score), font=('consolas', 40))
label.pack()

high_score_label = Label(window, text="High Score: 0", font=('consolas', 20))
high_score_label.pack()

canvas = Canvas(window, bg=BACKGROUND_COLOR, height=GAME_HEIGHT, width=GAME_WIDTH)
canvas.pack()

start_button = Button(window, text="Start", command=start_game, font=('consolas', 20))
start_button.pack()

try_again_button = Button(window, text="Try Again", command=start_game, font=('consolas', 20))

pause_button = Button(window, text="Pause", command=toggle_pause, font=('consolas', 20))
pause_button.pack()

window.update()

window_width = window.winfo_width()
window_height = window.winfo_height()
screen_width = window.winfo_screenwidth()
screen_height = window.winfo_screenheight()

x = int((screen_width / 2) - (window_width / 2))
y = int((screen_height / 2) - (window_height / 2))

window.geometry(f"{window_width}x{window_height}+{x}+{y}")

window.bind('<Left>', lambda event: change_direction('left'))
window.bind('<Right>', lambda event: change_direction('right'))
window.bind('<Up>', lambda event: change_direction('up'))
window.bind('<Down>', lambda event: change_direction('down'))

window.mainloop()
