from flask import Flask, request
import json
import logging
import os
from pathlib import Path
import subprocess
import time

app = Flask(__name__)
GENERATED_PATH = Path(os.getcwd()) / "generated"
os.makedirs(GENERATED_PATH, exist_ok=True)

logging.basicConfig(
    level=logging.INFO,
    format="[%(asctime)s] [%(process)d] [%(levelname)s] %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S %z",
)


@app.route("/gradle", methods=["POST"])
def run_gradle():
    """Runs the Gradle build script."""

    projectId = request.json.get("projectId")
    table = request.json.get("table")
    statement = request.json.get("statement")

    # we need all parameters to continue
    if not all((projectId, table, statement)):
        return {"error": "Missing a required parameter."}, 400

    cmd = [
        "gradle",
        "--quiet",
        "--console=plain",
        "run",
        # arguments passed to the Gradle build script
        f"-PappArgs={projectId}__SEP__{table}__SEP__{statement}",
    ]

    logging.info(statement)

    result = subprocess.run(
        cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True
    )

    stdout = result.stdout
    stderr = result.stderr

    ast = {"projectId": projectId, "table": table, "statement": statement}

    if stdout:
        ast["output"] = stdout
    elif stderr:
        logging.error(stderr)
        return {"error": stderr}, 500

    logging.info("\n" + stdout)

    with open(f"{GENERATED_PATH}/{projectId}.{table}-{time.time()}.json", "w+") as file:
        json.dump(ast, file)

    return ast, 200
