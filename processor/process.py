import logging
import os

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__file__)

input_folder = os.path.abspath(os.path.join(os.path.dirname(__file__), "input"))
output_folder = os.path.abspath(os.path.join(os.path.dirname(__file__), "output"))

logger.info(f"IN -> {input_folder}")
logger.info(f"OUT -> {output_folder}")

for in_item in os.listdir(input_folder):
  if in_item.startswith("."):
    continue
  logger.info(f"Processing {in_item}")
  with open(os.path.join(output_folder, f"{in_item}.json"), "w") as out_content:
    out_content.write("[\n")
    with open(os.path.join(input_folder, in_item), "r") as in_content:
      is_first_line = True
      while True:
        line = in_content.readline()
        if not line:
          break

        content = line.strip()

        if not is_first_line:
          content = f",\n{content}"
        is_first_line = False

        out_content.write(content)
    out_content.write("\n]")
  os.remove(os.path.join(input_folder, in_item))
