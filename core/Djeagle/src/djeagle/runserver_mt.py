import os

#print "file:", __file__
#DJANGO_PROJECT_DIR = os.path.dirname(__file__)
#print DJANGO_PROJECT_DIR
#PARENT_DIR, DJANGO_PROJECT_NAME = DJANGO_PROJECT_DIR.rsplit(os.sep, 1)
 
#sys.path.append(DJANGO_PROJECT_DIR)
os.environ['DJANGO_SETTINGS_MODULE'] = 'djeagle.settings'

HOST = "0.0.0.0"
PORT = 8000

import django.core.handlers.wsgi

application = django.core.handlers.wsgi.WSGIHandler()

if __name__ == '__main__':
    from ngniutils.net.httplib.server.wsgi import ThreadingWSGIServer
    httpd = ThreadingWSGIServer((HOST, PORT), application)
    
    print 'Serving on port %s:%d' % (HOST, PORT)
    httpd.serve_forever()