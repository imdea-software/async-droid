#! /usr/bin/env python
# -*- coding: utf-8 -*-
'''
Copyright (C) 2013  Diego Torres Milano
Created on 2014-06-16 by Culebra v7.1.1
                      __    __    __    __
                     /  \  /  \  /  \  /  \ 
____________________/  __\/  __\/  __\/  __\_____________________________
___________________/  /__/  /__/  /__/  /________________________________
                   | / \   / \   / \   / \   \___
                   |/   \_/   \_/   \_/   \    o \ 
                                           \_____/--<
@author: Diego Torres Milano
@author: Jennifer E. Swofford (ascii art snake)
'''


import re, sys, os

from com.dtmilano.android.viewclient import ViewClient


kwargs1 = {'verbose': True, 'ignoresecuredevice': False}
device, serialno = ViewClient.connectToDeviceOrExit(**kwargs1)
kwargs2 = {'startviewserver': True, 'forceviewserveruse': False, 'autodump': False, 'ignoreuiautomatorkilled': True}
vc = ViewClient(device, serialno, **kwargs2)
vc.dump(window='-1')

'''
# class=android.widget.FrameLayout
id_no_id_1 = vc.findViewByIdOrRaise("id/no_id/1")

# class=android.widget.LinearLayout
id_no_id_2 = vc.findViewByIdOrRaise("id/no_id/2")

# class=android.widget.FrameLayout
id_no_id_3 = vc.findViewByIdOrRaise("id/no_id/3")

# class=android.view.View
id_no_id_4 = vc.findViewByIdOrRaise("id/no_id/4")

# class=android.widget.FrameLayout
id_no_id_5 = vc.findViewByIdOrRaise("id/no_id/5")

# class=android.widget.ImageView
id_no_id_6 = vc.findViewByIdOrRaise("id/no_id/6")

# class=android.widget.LinearLayout
id_no_id_7 = vc.findViewByIdOrRaise("id/no_id/7")

# class=android.widget.LinearLayout
id_no_id_8 = vc.findViewByIdOrRaise("id/no_id/8")

# class=android.widget.TextView text=u'HelloWorldApp'
id_no_id_9 = vc.findViewByIdOrRaise("id/no_id/9")

# class=android.widget.LinearLayout
id_no_id_10 = vc.findViewByIdOrRaise("id/no_id/10")

# class=android.widget.ImageButton
id_no_id_11 = vc.findViewByIdOrRaise("id/no_id/11")

# class=android.widget.FrameLayout
id_no_id_12 = vc.findViewByIdOrRaise("id/no_id/12")

# class=android.widget.FrameLayout
id_no_id_13 = vc.findViewByIdOrRaise("id/no_id/13")

# class=android.widget.LinearLayout
id_no_id_14 = vc.findViewByIdOrRaise("id/no_id/14")

# class=android.widget.LinearLayout
id_no_id_15 = vc.findViewByIdOrRaise("id/no_id/15")

# class=android.widget.EditText text=u'Blank Text Field'
id_no_id_16 = vc.findViewByIdOrRaise("id/no_id/16")

# class=android.widget.Button text=u'processUI'
id_no_id_17 = vc.findViewByIdOrRaise("id/no_id/17")
if id_no_id_17.clickable():

# class=android.widget.LinearLayout
id_no_id_18 = vc.findViewByIdOrRaise("id/no_id/18")

# class=android.widget.EditText text=u'Blank Text Field'
id_no_id_19 = vc.findViewByIdOrRaise("id/no_id/19")

# class=android.widget.Button text=u'sendMessage'
id_no_id_20 = vc.findViewByIdOrRaise("id/no_id/20")

# class=android.widget.LinearLayout
id_no_id_21 = vc.findViewByIdOrRaise("id/no_id/21")

# class=android.widget.EditText text=u'Blank Text Field'
id_no_id_22 = vc.findViewByIdOrRaise("id/no_id/22")

# class=android.widget.Button text=u'postRunnable'
id_no_id_23 = vc.findViewByIdOrRaise("id/no_id/23")

# class=android.widget.LinearLayout
id_no_id_24 = vc.findViewByIdOrRaise("id/no_id/24")

# class=android.widget.EditText text=u'Blank Text Field'
id_no_id_25 = vc.findViewByIdOrRaise("id/no_id/25")

# class=android.widget.Button text=u'newAsyncTask'
id_no_id_26 = vc.findViewByIdOrRaise("id/no_id/26")

# class=android.widget.LinearLayout
id_no_id_27 = vc.findViewByIdOrRaise("id/no_id/27")

# class=android.widget.EditText text=u'Blank Text Field'
id_no_id_28 = vc.findViewByIdOrRaise("id/no_id/28")

# class=android.widget.Button text=u'AsyncTaskExecutor'
id_no_id_29 = vc.findViewByIdOrRaise("id/no_id/29")

# class=android.widget.LinearLayout
id_no_id_30 = vc.findViewByIdOrRaise("id/no_id/30")

# class=android.widget.EditText text=u'Blank Text Field'
id_no_id_31 = vc.findViewByIdOrRaise("id/no_id/31")

# class=android.widget.Button text=u'newThread'
id_no_id_32 = vc.findViewByIdOrRaise("id/no_id/32")
'''

for id in vc.getViewIds():
    myView =  vc.findViewById(id)
#    print "button: ", myView.getClass(), myView.getId(), myView.getCoords()
    if myView.isClickable():
	    myView.touch()
	    device.type('sometext')
    
	