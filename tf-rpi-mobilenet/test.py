import time
import keras
from scipy import misc
import numpy as np

start = time.time()
model = keras.applications.mobilenet.MobileNet(alpha=1.0, depth_multiplier=1, dropout=1e-3, include_top=True, weights=None, input_tensor=None, pooling=None, classes=1000)
model.load_weights('mobilenet.h5')
end = time.time()
print('Load:', end - start)

img = misc.imread('input.bmp')
img = img / 255
img = np.reshape(img, (1,) + img.shape)

start = time.time()
results = model.predict(img)
end = time.time()
print('Predict:', end - start)
