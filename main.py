import os

from zetasql_python import app

GENERATED_PATH = "generated"


def start_flask_app(debug: bool = False) -> None:
    """Starts the Flask server."""
    os.makedirs(GENERATED_PATH, exist_ok=True)

    app.run(debug=debug)


if __name__ == "__main__":
    start_flask_app()
