import time
from model import *
from scipy import misc

start = time.time()
model = unet()
model.load_weights('unet_128.hdf5')
end = time.time()
print('Load:', end - start)

img = misc.imread('input.bmp')
img = img / 255
img = np.reshape(img, img.shape + (1,))
img = np.reshape(img, (1,) + img.shape)

start = time.time()
results = model.predict(img)
end = time.time()
print('Predict:', end - start)

results = np.reshape(results, (128, 128))
results = results * 255
misc.imsave('output.bmp', results, 'bmp')

