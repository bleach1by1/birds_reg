import numpy as np
import cv2
import skimage.draw
import os
import tensorflow as tf
import config
from libtiff import TIFF
import xlrd


class birdsModel(object):
  """Class to load birds model and run inference."""
  INPUT_TENSOR_NAME  = 'ImageTensor:0'
  OUTPUT_TENSOR_NAME = 'SemanticPredictions:0'
  
  
  def __init__(self):      
    """Creates and loads pretrained birds model."""
    self.graph = tf.Graph()

    graph_def = None

    with open(config.TEST_PB_PATH, 'rb') as fhandle:
        graph_def = tf.GraphDef.FromString(fhandle.read())

    if graph_def is None:
      raise RuntimeError('Cannot find inference graph in tar archive.')

    with self.graph.as_default():
      tf.import_graph_def(graph_def, name='')

    self.sess = tf.Session(graph=self.graph)
    
    self.dic = {}
    print(config.TEST_COLOR_URL)
    workbook = xlrd.open_workbook(config.TEST_COLOR_URL)
    index = workbook.sheet_names()[0]
    print(index)
    worksheet = workbook.sheet_by_name(index)
    nrows = worksheet.nrows
    ncols = worksheet.ncols
    begin = 1
    for i in range(1,nrows):
        self.dic[begin] = [int(worksheet.cell(i,1).value),
                      int(worksheet.cell(i,2).value),
                      int(worksheet.cell(i,3).value)]
        begin+=1

    

  def draw_line(self, image):
    uniques = np.unique(image)
    for i in uniques:
        img_temp = np.zeros(image.shape,dtype=np.uint8)
        img_temp[image==i] = 255
        contours, hierarchy = cv2.findContours(img_temp,cv2.RETR_TREE,cv2.CHAIN_APPROX_SIMPLE)
        cv2.drawContours(image,contours,-1,255,1)
    image[image<255] = 0
    return image
    
  def draw_color(self, image):
    img_temp = np.zeros([image.shape[0],image.shape[1],3],dtype=np.uint8)
    uniques = np.unique(image)
    print(uniques)
    for i in uniques:
        if i>0:
            print(i)
            print(self.dic[i])       
            img_temp[image==i] = self.dic[i]
    return img_temp
    
  def run(self, image):
    """Runs inference on a single image.

    Args:
      image: raw input image.

    Returns:
      resized_image: RGB image resized from original input image.
      seg_map: Segmentation map of `resized_image`.
    """
    batch_seg_map = self.sess.run(
        self.OUTPUT_TENSOR_NAME,
        feed_dict={self.INPUT_TENSOR_NAME: [np.asarray(image)]})
    print(batch_seg_map[0].shape)
    bb = batch_seg_map[0]
    uniques = np.unique(bb)
    mask = np.zeros(bb.shape,dtype=np.uint8)
    for un in uniques:
        mask[bb==un] = un
    return mask


import argparse
if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--TEST_IMAGE_PATH', required=False,
                            metavar="/path/to/image.tif",
                            help='Directory of the validation brain dataset')
    parser.add_argument('--TEST_OUT_PATH', required=False,
                            metavar="/path/to/out/",
                            help='Directory of the out file')
    parser.add_argument('--TEST_PB_PATH', required=False,
                            metavar="/path/to/.pb",
                            help='Directory of the PB dataset(from export model)')
    parser.add_argument('--TEST_COLOR_URL', required=False,
                            metavar="/path/to/color.xls",
                            help='Directory of the brain region color')
    args = parser.parse_args()
    print(args.TEST_IMAGE_PATH)

    config.TEST_IMAGE_PATH = args.TEST_IMAGE_PATH
    config.TEST_OUT_PATH = args.TEST_OUT_PATH
    config.TEST_PB_PATH = args.TEST_PB_PATH
    config.TEST_COLOR_URL = args.TEST_COLOR_URL

    MODEL = birdsModel()

    open_tiff = TIFF.open(config.TEST_IMAGE_PATH)

    out_org_url = config.TEST_OUT_PATH+'out_org.tif'
    out_org_tiff = TIFF.open(out_org_url,mode='w')
    out_line_url = config.TEST_OUT_PATH+'out_line.tif'
    out_line_tiff = TIFF.open(out_line_url,mode='w')
    out_color_url = config.TEST_OUT_PATH+'out_color.tif'
    out_color_tiff = TIFF.open(out_color_url,mode='w')
    line_stack = []
    color_stack = []
    for img in list(open_tiff.iter_images()):
        print(img.shape)
        org_image = MODEL.run(img)
        out_org_tiff.write_image(org_image,compression = None, write_rgb = True)
        line_image = np.array(org_image)
        color_image = np.array(org_image)
        line_stack.append(MODEL.draw_line(color_image))
        color_stack.append(MODEL.draw_color(line_image))

    out_org_tiff.close()

    for img in color_stack:
        out_color_tiff.write_image(img,compression = None, write_rgb = True)
    out_color_tiff.close()

    for img in line_stack:
        out_line_tiff.write_image(img,compression = None, write_rgb = True)
    out_line_tiff.close()

    print('model loaded successfully!')