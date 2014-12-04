import os

os.chdir('../monitor')
os.system('python -m tornado.testing --verbose tests.runtests')
