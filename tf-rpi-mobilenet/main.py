import keras
import numpy as np
from keras.applications import mobilenet
from keras.preprocessing.image import load_img
from keras.preprocessing.image import img_to_array, array_to_img
from keras.applications.imagenet_utils import decode_predictions
from PIL import Image
import time
import psutil

def get_image(number):
	filename = 'test/{0}.bmp'.format(number)
	original = load_img(filename, target_size=(224, 224))
	numpy_image = img_to_array(original)
	image_batch = np.expand_dims(numpy_image, axis=0)

	processed_image = mobilenet.preprocess_input(image_batch.copy())
	#processed = array_to_img(processed_image[0])
	#im = processed
	#im.save(filename)
	return processed_image

def show_image(number):
	filename = 'test/{0}.bmp'.format(i)
	image = Image.open(filename)
	image.show()

	time.sleep(3)

	for proc in psutil.process_iter():
		if proc.name() == "display":
			proc.kill()

start = time.time()
mobilenet_model = mobilenet.MobileNet()
mobilenet_model.load_weights('mobilenet.h5')
end = time.time()
print('Load:', end - start)

for i in range(0, 51):
	start = time.time()
	predictions = mobilenet_model.predict(get_image(i))
	end = time.time()
	print('Predict:', end - start)

	label = decode_predictions(predictions)
	print(predictions.argmax())

	#show_image(i)

