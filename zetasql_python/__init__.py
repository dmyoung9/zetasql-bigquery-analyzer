from flask import Flask, request
import json
import subprocess
import time

app = Flask(__name__)
GENERATED_PATH = "generated"


@app.route("/gradle", methods=["POST"])
def run_gradle():
    """Runs the Gradle build script."""

    projectId = request.json.get("projectId")
    table = request.json.get("table")
    statement = request.json.get("query")

    cmd = [
        "gradle",
        "--quiet",
        "--console=plain",
        "run",
        # arguments passed to the Gradle build script
        f'-PappArgs="{projectId}","{table}","{statement}"',
    ]
    result = subprocess.run(
        cmd,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )

    stdout = result.stdout
    stderr = result.stderr

    ast = {
        "projectId": projectId,
        "table": table,
        "statement": statement,
    }
    if stdout:
        ast["output"] = stdout
    if stderr:
        ast["error"] = stderr

    with open(f"{projectId}.{table}-{time.time()}.json", "w+") as file:
        json.dump(ast, file)

    return ast
