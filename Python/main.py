import numpy as np
import torch
import torch.nn as nn
import cv2
import matplotlib.pyplot as plt
from skimage.data import shepp_logan_phantom
from skimage.metrics import peak_signal_noise_ratio
from skimage import io
import os
from skimage.util import random_noise

colored_images = []
grayscale_images = []
val_images = []


def display(window_name, img, x, y):
    cv2.imshow(window_name, img * 1.0 / (np.max(img) + 1e-15))
    cv2.moveWindow(window_name, x, y)


def display_colored(window_name, img, x, y):
    # Convert image to uint8 for display
    cv2.imshow(window_name, img)
    cv2.moveWindow(window_name, x, y)


def display_noisy(window_name, img, x, y):
    img_display = np.clip(img, 0, 255).astype(np.uint8)
    cv2.imshow(window_name, img_display * 1.0 / (np.max(img) + 1e-15))
    cv2.moveWindow(window_name, x, y)


def add_gaussian_noise(image, sigma=25):
    gaussian_noise = np.random.normal(0, sigma, image.shape).astype(np.float32)
    noisy_image = np.clip(image + gaussian_noise, 0, 255).astype(np.float32)
    return noisy_image


class Convolution_NxN(nn.Module):
    def __init__(self, kernel_size):
        super(Convolution_NxN, self).__init__()
        self.convlkernel = nn.Conv2d(1, 1, kernel_size, padding=(int(kernel_size / 2), int(kernel_size / 2)),
                                     bias=False)
        # self.convlkernel.weight.data.fill_(1.0/(kernel_size*kernel_size))

    def forward(self, x):
        x = self.convlkernel(x)
        return x


class ColorCNN(nn.Module):
    def __init__(self):
        super(ColorCNN, self).__init__()
        self.ColorCNN = nn.Sequential(
            nn.Conv2d(3, 8, 3, padding=1), nn.PReLU(),
            nn.Conv2d(8, 16, 3, padding=1), nn.PReLU(),
            nn.Conv2d(16, 32, 3, padding=1), nn.PReLU(),
            nn.Conv2d(32, 16, 3, padding=1), nn.PReLU(),
            nn.Conv2d(16, 3, 3, padding=1), nn.PReLU()
        )

    def forward(self, x):
        x = self.ColorCNN(x)
        return x


def plots(train_loss_values, val_loss_values, train_psnr_values, val_psnr_values):
    num_iterations = max(len(train_loss_values), len(val_loss_values))

    plt.figure(figsize=(12, 5))

    # Plot Training Loss
    plt.plot(range(num_iterations), train_loss_values, label='Training Loss')

    # Plot Validation Loss
    plt.plot(range(num_iterations), val_loss_values, label='Validation Loss', color='orange')

    plt.xlabel('Iteration')
    plt.ylabel('Loss')
    plt.title('Training and Validation Loss')
    plt.legend()

    plt.tight_layout()
    plt.savefig('training_plots.png')  # Save the training loss plot

    # Plot PSNR
    plt.figure(figsize=(12, 5))
    plt.plot(range(num_iterations), train_psnr_values, label='Training PSNR')
    plt.plot(range(num_iterations), val_psnr_values, label='Validation PSNR', color='orange')
    plt.xlabel('Iteration')
    plt.ylabel('PSNR')
    plt.title('Training and Validation PSNR')
    plt.legend()
    plt.tight_layout()
    plt.savefig('psnr_plots.png')  # Save the PSNR plot

    plt.show()  # Display the plots


def calculate_psnr(output, target):
    output_np = output[0].detach().numpy().astype(np.uint8)
    target_np = target[0].detach().numpy().astype(np.uint8)
    psnr_value = peak_signal_noise_ratio(target_np.transpose(1, 2, 0), output_np.transpose(1, 2, 0), data_range=255)
    return psnr_value


def colored_train(images, val_images, batch_size=8, num_epochs=100, learning_rate=0.003,
                  save_path='color_cnn_model_22.01.pth'):
    preprocessed_images = preprocess_images(images, target_size=(256, 256))
    val_preprocessed_images = preprocess_images(val_images, target_size=(256, 256))
    color_cnn = ColorCNN()
    loss_function = nn.MSELoss()
    optimizer = torch.optim.Adam(color_cnn.parameters(), lr=learning_rate)
    loss_values = []
    val_loss_values = []
    learning_rates = []
    psnr_values = []
    val_psnr_values = []

    start_epoch = 0

    if os.path.exists(save_path):
        color_cnn.load_state_dict(torch.load(save_path))
        print(f"Loaded model from {save_path}")
        start_epoch = int(os.path.splitext(os.path.basename(save_path))[0].split('_')[-1])

    for epoch in range(start_epoch, num_epochs):
        np.random.shuffle(preprocessed_images)
        for i in range(0, len(preprocessed_images), batch_size):
            batch_images = preprocessed_images[i:i + batch_size]
            # Check if the image is grayscale
            is_grayscale = all(np.all(np.std(img, axis=2) < 1e-3) for img in batch_images)
            target_batch = torch.stack(
                [torch.from_numpy(img.astype(np.float32)).permute(2, 0, 1) for img in batch_images])

            if is_grayscale:
                # For grayscale images, make sure the three channels have equal values
                target_batch = torch.stack(
                    [torch.from_numpy(img[:, :, 0].astype(np.float32)).repeat(3, 1, 1) for img in batch_images])
                print("E GRAYSCALE")
            noisy_batch_np = [add_gaussian_noise(img) for img in batch_images]
            noisy_batch_torch = torch.stack(
                [torch.from_numpy(noisy_np).permute(2, 0, 1) for noisy_np in noisy_batch_np])

            # print(image)
            # print(noisy_np)
            display_colored("Original", cv2.resize(batch_images[0], (x, x), interpolation=0), 0, 0)
            display_noisy("Noisy", cv2.resize(noisy_batch_np[0], (x, x), interpolation=0), x, 0)

            current_output_batch = color_cnn(noisy_batch_torch)
            current_output_batch = torch.clamp(current_output_batch, 0, 255)
            # print(current_output)
            loss = loss_function(current_output_batch, target_batch)
            loss.backward()

            optimizer.step()
            optimizer.zero_grad()

            for j in range(len(batch_images)):
                cnn_output = np.squeeze(current_output_batch[j].detach().numpy()).transpose(1, 2, 0)
                # print(cnn_output)
                cnn_output_display = cnn_output.astype(np.uint8)
                cnn_output_display_umat = cv2.UMat(cnn_output_display)
                cv2.putText(cnn_output_display_umat, f"Epoch {epoch}, Batch {i // batch_size}", (2, 30), 0, 1,
                            int(np.max(cnn_output_display) + 1),
                            1, cv2.LINE_AA)
                display_colored("Output after better conv", cv2.resize(cnn_output_display, (x, x), interpolation=0), x,
                                x + 30)
                cv2.waitKey(1)
        train_psnr = calculate_psnr(current_output_batch, target_batch)
        psnr_values.append(train_psnr)
        learning_rates.append(optimizer.param_groups[0]['lr'])
        loss_values.append(loss.item())
        # Validation loop
        val_loss = 0.0
        with torch.no_grad():
            for i in range(0, len(val_preprocessed_images), batch_size):
                val_batch_images = val_preprocessed_images[i:i + batch_size]
                val_target_batch = torch.stack(
                    [torch.from_numpy(img.astype(np.float32)).permute(2, 0, 1) for img in val_batch_images])

                val_noisy_batch_np = [add_gaussian_noise(img) for img in val_batch_images]
                val_noisy_batch_torch = torch.stack(
                    [torch.from_numpy(noisy_np).permute(2, 0, 1) for noisy_np in val_noisy_batch_np])

                val_output_batch = color_cnn(val_noisy_batch_torch)
                val_output_batch = torch.clamp(val_output_batch, 0, 255)
                val_loss += loss_function(val_output_batch, val_target_batch).item()
        val_psnr = calculate_psnr(val_output_batch, val_target_batch)
        val_psnr_values.append(val_psnr)

        val_loss /= len(val_preprocessed_images)  # Average validation loss
        val_loss_values.append(val_loss)
        if epoch % 10 == 0:
            torch.save(color_cnn.state_dict(), f"{save_path.replace('.pth', '')}_{epoch:03d}.pth")
            print(f"Model saved after epoch {epoch}")
    if epoch == num_epochs - 1:
        print("Values on each channel of the last image:")
        for ch in range(target_batch.shape[1]):
            print(f"Channel {ch + 1}:")
            print(target_batch[-1, ch, :, :])

    plots(loss_values, val_loss_values, psnr_values, val_psnr_values)


def colored_test(x):
    # color_cnn = ColorCNN();
    # color_cnn.load_state_dict(torch.load('model.pt'))
    color_cnn = torch.jit.load('color_model_18.01.pt')
    color_cnn.eval()

    test_image = cv2.imread('D:/LICENTA/taj-rgb-noise.jpg')  # BGR
    # test_image = cv2.resize(test_image, (x, x), interpolation=0)
    test_image_tensor = torch.from_numpy(test_image.astype(np.float32)).permute(2, 0, 1).unsqueeze(0)  # c, h, w
    shape = test_image_tensor.shape
    # print(shape)  # [1,3,195,188]
    tensor_values = test_image_tensor.numpy()
    # print(tensor_values)
    with torch.no_grad():
        model_output = color_cnn(test_image_tensor)
    original_image_display = cv2.resize(test_image, (x, x), interpolation=0)
    colorized_output = torch.clamp(model_output, 0, 255)
    print(colorized_output)
    colorized_output_np = np.squeeze(colorized_output.numpy()).transpose(1, 2, 0).astype(np.uint8)  # h, w, c
    colorized_output_display = cv2.resize(colorized_output_np, (x, x), interpolation=0)

    display_colored("Original", original_image_display, 0, 0)
    display_colored("Colorized Output", colorized_output_display, x, 0)


def preprocess_images(images, target_size=(256, 256)):
    # Resize or crop images to the target size
    processed_images = []
    for img in images:
        processed_img = cv2.resize(img, target_size, interpolation=cv2.INTER_CUBIC)
        processed_images.append(processed_img)
    return processed_images


def add_images_to_training_dataset(dir, image_list):
    for filename in os.listdir(dir):
        img = cv2.imread(os.path.join(dir, filename))

        if img is not None:
            # Ensure that the image has 3 channels
            if img.shape[2] == 3:
                print("Au 3 canale")
                # Check if all channels have the same values
                is_grayscale = np.all(np.std(img, axis=2) < 1e-3)

                if is_grayscale:
                    print("Sunt grayscale tho")
            image_list.append(img)


if __name__ == '__main__':
    # file = os.path.join(skimage.data_dir, 'rose.jpg')

    ##
    # training_dir_name = 'D:/LICENTA/NewCNN/grey_dataset'
    # images = []
    # for filename in os.listdir(training_dir_name):
    #     original_np = io.imread(os.path.join(training_dir_name, filename), as_gray=True)
    #     if original_np is not None:
    #         images.append(original_np)

    bsds300_train = 'D:/LICENTA/datasets/BSDS300/images/train'
    add_images_to_training_dataset(bsds300_train, colored_images)

    # denoising_dataset_high = 'D:/LICENTA/datasets/Denoising_Dataset/Training/high'
    # add_images_to_training_dataset(denoising_dataset_high, colored_images)
    #
    # denoising_dataset_low = 'D:/LICENTA/datasets/Denoising_Dataset/Training/low'
    # add_images_to_training_dataset(denoising_dataset_low, colored_images)

    image_net = 'D:/LICENTA/datasets/ImageNet/train'
    add_images_to_training_dataset(image_net, colored_images)

    real_world_noisy_images = 'D:/LICENTA/datasets/Real World Noisy Images'
    add_images_to_training_dataset(real_world_noisy_images, colored_images)

    bsds300_validation = 'D:/LICENTA/datasets/BSDS300/images/test'
    add_images_to_training_dataset(bsds300_validation, val_images)

    image_net_validation = 'D:/LICENTA/datasets/ImageNet/val'
    add_images_to_training_dataset(image_net_validation, val_images)
    #

    # add_images_to_training_dataset('D:/LICENTA/NewCNN/grey_dataset', grayscale_images)

    # for filename in os.listdir(colored_training_dir_name):
    #     img = cv2.imread(os.path.join(colored_training_dir_name, filename))
    #     # numpy_data = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    #     if img is not None:
    #         # if img.shape[2] == 4:
    #         #     img = img[:, :, :3]
    #         colored_images.append(img)

    original_np = shepp_logan_phantom()
    x = original_np.shape[0]

    colored_train(colored_images, val_images)
    # colored_train(grayscale_images)
    # colored_test(x)


cv2.waitKey(0)
