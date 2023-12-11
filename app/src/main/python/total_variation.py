import cv2
import numpy as np
from skimage.restoration import denoise_tv_chambolle, denoise_wavelet
from skimage import img_as_ubyte
from android.util import Log
import base64
from PIL import Image
import io
# import bm3d
# from pywt import wavedec2, waverec2

def total_variation(data):
    decoded_data = base64.b64decode(data)
    np_data = np.fromstring(decoded_data, np.uint8)
    img = cv2.imdecode(np_data, cv2.IMREAD_UNCHANGED)

    denoised_img = denoise_tv_chambolle(img, weight=0.1, multichannel=True)
    denoised_img = img_as_ubyte(denoised_img)
    denoised_img = cv2.cvtColor(denoised_img, cv2.COLOR_BGR2RGB)
    pil_im = Image.fromarray(denoised_img)

    buff = io.BytesIO()
    pil_im.save(buff, format="PNG")
    img_str = base64.b64encode(buff.getvalue())
    return ""+str(img_str, 'utf-8')

def wavelet_denoising(data):
      decoded_data = base64.b64decode(data)
      np_data = np.fromstring(decoded_data, np.uint8)
      img = cv2.imdecode(np_data, cv2.IMREAD_UNCHANGED)

#       sigma_est = estimate_sigma(img, multichannel=True, average_sigmas=True)

      img_bayes = denoise_wavelet(img, method='BayesShrink', mode='soft', wavelet_levels=3, wavelet='coif5', multichannel=True, convert2ycbcr=True, rescale_sigma=True)
      denoised_img = img_as_ubyte(img_bayes)
      denoised_img = cv2.cvtColor(denoised_img, cv2.COLOR_BGR2RGB)
      pil_im = Image.fromarray(denoised_img)

      buff = io.BytesIO()
      pil_im.save(buff, format="PNG")
      img_str = base64.b64encode(buff.getvalue())
      return ""+str(img_str, 'utf-8')


# def bm3d_denoising(data):
# #     decoded_data = base64.b64decode(data)
# #     np_data = np.fromstring(decoded_data, np.uint8)
# #     img = cv2.imdecode(np_data, cv2.IMREAD_UNCHANGED)
# #
# #     denoised_img = denoise_bm3d(img, simgma_psd=0.2, multichannel=True)
# #     denoised_img = cv2.convertScaleAbs(denoised_img)
# #     pil_im = Image.fromarray(denoised_img)
# #
# #     buff = io.BytesIO()
# #     pil_im.save(buff, format="PNG")
# #     img_str = base64.b64encode(buff.getvalue())
# #     return ""+str(img_str, 'utf-8')
#       decoded_data = base64.b64decode(data)
#       np_data = np.fromstring(decoded_data, np.uint8)
#       img = cv2.imdecode(np_data, cv2.IMREAD_UNCHANGED)
#
#       bm3d_denoised = bm3d.bm3d(img, sigma_psd=0.2, stage_arg=bm3d.BM3DStages.ALL_STAGES)
#       denoised_img = img_as_ubyte(img_bayes)
#       denoised_img = cv2.cvtColor(denoised_img, cv2.COLOR_BGR2RGB)
#       pil_im = Image.fromarray(denoised_img)
#
#       buff = io.BytesIO()
#       pil_im.save(buff, format="PNG")
#       img_str = base64.b64encode(buff.getvalue())
#       return ""+str(img_str, 'utf-8')

