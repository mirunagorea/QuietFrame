from torch.utils.mobile_optimizer import optimize_for_mobile
from main import ColorCNN
import torch


def optimize_save():
    # Load in the model
    model = ColorCNN()
    model.load_state_dict(torch.load("color_cnn_model_22.01_090.pth",
                                     map_location=torch.device("cpu")))
    model.eval()  # Put the model in inference mode

    # Generate example inputs
    example_input = torch.randn(1, 3, 30, 50)

    # Generate the optimized model
    traced_script_module = torch.jit.trace(model, example_input)
    traced_script_module_optimized = optimize_for_mobile(
        traced_script_module)

    # Save the optimzied model
    traced_script_module_optimized._save_for_lite_interpreter(
        "color_model_22.01.pt")


if __name__ == '__main__':
    optimize_save()
