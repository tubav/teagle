#! /usr/bin/env python
'''
Created on 20.07.2010

@author: kca
'''

from weakref import WeakKeyDictionary
from threading import Lock, Thread
import logging
from ptm.notifications import NotificationId
from ptm.exc import CommunicationError
from Queue import Queue

logger = logging.getLogger("ptm")

class NotificationManager(Thread):
    def __init__(self, registry, *args, **kw):
        super(NotificationManager, self).__init__(*args, **kw)
        self.__pending = WeakKeyDictionary()
        #TODO: Locking needed here?
        self.__lock = Lock()
        self.__registry = registry
        self.__queue = Queue()
        self.__running = True
        
    def notify(self, condition, owner, reference):
        try:
            entry = self.__registry.resolve_payload_entry(owner)
            self.__enqueue(condition, entry, reference)
            self.__queue.put(owner)
        except:
            logger.exception("Failed to resolve owner for notification: " + str(owner))
        
    def __do_notify(self, condition, owner, entry, reference):
        if entry.is_available:
            left = condition
            shadow = self.__registry.shadow_manager.get_shadow(entry.payload)
            for c in condition:
                try:
                    shadow.notify(c, owner, reference)
                    left = left & (~c)
                except CommunicationError:
                    logger.exception("unable to notify " + str(shadow))
                    break
                
                if not left:
                    return
                condition = NotificationId(left)
    
        self.__enqueue(condition, entry, reference)
        
    def __enqueue(self, condition, owner_entry, reference):
        logger.debug("queuing message")
        with self.__lock:
            for c in condition:
                val = (c, reference)
                try:
                    queue = self.__pending[owner_entry]
                    if val not in queue:
                        queue.append(val)
                except KeyError:
                    self.__pending[owner_entry] = [ val ]
                
    def remove_reference(self, reference):
        with self.__lock:
            for owner_entry, queue in self.__pending.items():
                for c, r in tuple(queue):
                    if r == reference:
                        queue.remove((c, r))
                
                if not queue:
                    self.__pending.pop(owner_entry, None)

    def join(self, timeout = None):
        self.__running = False
        super(NotificationManager, self).join(timeout)

    def run(self):
        while self.__running:
            try:
                owner_entry = None
            except (SystemExit, KeyboardInterrupt):
                self.__running = False
            except:
                logger.exception("Error during notify loop")