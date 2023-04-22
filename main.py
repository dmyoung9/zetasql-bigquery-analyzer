from pathlib import Path
import os

from zetasql_python import app

GENERATED_PATH = Path(os.getcwd()) / "generated"


def start_flask_app(debug: bool = False) -> None:
    """Starts the Flask server."""
    os.makedirs(GENERATED_PATH, exist_ok=True)

    app.run(host="0.0.0.0", port=5000, debug=debug)


if __name__ == "__main__":
    start_flask_app()
