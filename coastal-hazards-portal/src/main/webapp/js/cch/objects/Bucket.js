/*jslint browser: true*/
/*global $*/
/*global CCH*/
CCH.Objects.Bucket = function (args) {
	"use strict";
	CCH.LOG.trace('CCH.Objects.Bucket::constructor: Bucket class is initializing.');

	var me = (this === window) ? {} : this;
	me.bucketLoadEvent = 'svg-bucket-loaded';
	me.slide = args.slide;
	me.$BUCKET_CONTAINER_CONTROL_CONTAINER_ID = $('#app-navbar-bucket-control-container');
	me.BUCKET_COUNT_CONTAINER_ID = 'app-navbar-bucket-button-count';
	me.BUCKET_CONTAINER_ID = 'app-navbar-bucket-button-container';
	me.BUCKET_POPULATED_CLASS = 'app-navbar-bucket-button-container-populated';
	me.BUCKET_UNPOPULATED_CLASS = 'app-navbar-bucket-button-container-unpopulated';
	me.MARGIN_WIDTH = 0;
	me.bucket = [];
	me.bucketContainer = document.getElementById('animated-bucket-object');
	me.bucketSVG = null;
	
	try {
		me.bucketSVG = me.bucketContainer.getSVGDocument();
	} catch (err) {
		// This is a fix primarily for Internet Explorer 9 and its issues with 
		// loading SVGs. It fires an exception on .getSVGDocument() and seems to 
		// load it down the line (in the .onload() event). http://i.imgur.com/gyYuele.png
		CCH.LOG.debug("SVG loading issue encountered. Can probably keep going");
	}
	
	// Depending on the browser, the bucket SVG may be loaded at this time
	if (me.bucketSVG) {
		$(document).trigger(me.bucketLoadEvent);
	}
	
	// Otherwise, we need to attach the onload event to the Object node
	me.bucketContainer.onload = function () {
		me.bucketSVG = this.getSVGDocument();
		$(document).trigger(me.bucketLoadEvent);
	};

	me.bucketAddClickHandler = function (evt, args) {
		args = args || {};
		var item = args.item,
			visibility = args.visibility || false;

		if (item) {
			me.add({
				item: item,
				visibility: visibility
			});
		}
	};
	me.bucketRemoveClickHandler = function (evt, args) {
		args = args || {};
		var id = args.id,
			item = id ? CCH.items.getById({id: id}) : args.item;

		if (item) {
			item.hideLayer();
			me.remove({
				item: item
			});
		}
	};

	me.countChanged = function () {
		var count = me.getCount(),
			bucketContainer = $('#' + me.BUCKET_CONTAINER_ID);

		if (count > 0) {
			if (!bucketContainer.hasClass(me.BUCKET_POPULATED_CLASS)) {
				bucketContainer.removeClass(me.BUCKET_UNPOPULATED_CLASS);
				bucketContainer.addClass(me.BUCKET_POPULATED_CLASS);
			}
		} else {
			bucketContainer.removeClass(me.BUCKET_POPULATED_CLASS);
			bucketContainer.addClass(me.BUCKET_UNPOPULATED_CLASS);
		}
		
		CCH.LOG.debug('CCH.Objects.Bucket::countChanged: Bucket count changed. Current count: ' + count);
		
		return count;
	};

	$('#' + me.BUCKET_CONTAINER_ID).on('click', function () {
		$(window).trigger('app-navbar-button-clicked');
		ga('send', 'event', {
			'eventCategory': 'bucket',
			'eventAction': 'bucketClicked',
			'eventLabel': 'bucket image'
		});
	});

	$(window).on({
		'cch.card.bucket.add': me.bucketAddClickHandler,
		'cch.slide.search.button.bucket.add': me.bucketAddClickHandler,
		'cch.card.bucket.remove': me.bucketRemoveClickHandler,
		'cch.slide.bucket.remove': me.bucketRemoveClickHandler,
		'bucket-remove': me.bucketRemoveClickHandler
	});
	
	$(document).on(me.bucketLoadEvent, function () {
		$(window).on({
			'cch.slide.bucket.opening' : function ()  { me.bucketSVG.bucketOpen(); },
			'cch.slide.bucket.closing' : function ()  { me.bucketSVG.bucketClose(); }
		});
	});

	CCH.LOG.trace('CCH.Objects.Bucket::constructor: Bucket class initialized.');

	return $.extend(me, {
		bucketLoadEvent : me.bucketLoadEvent,
		bucketSVG: me.bucketSVG,
		add: function (args) {
			args = args || {};

			// Make sure I get what I need to remove
			if (!args.item) {
				throw "item not passed to CCH.Objects.Bucket";
			}

			var item = args.item,
				id = item.id,
				visibility = args.visibility;

			if (!me.getItemById(id)) {
				
				// Add the item to my personal bucket array
				me.bucket.push(item);

				// Add the item to the bucket slide
				me.slide.add({
					item: item,
					visibility: visibility
				});

				// Add the item to the session
				CCH.session.addItem({
					item: item,
					visible: visibility
				});

				// Increase the bucket count visually
				me.increaseCount();

				// Trigger the addition in the window
				$(window).trigger('bucket-added', {
					id: id
				});
			}

			return me.bucket;
		},
		remove: function (args) {
			args = args || {};

			if (!args.item && !args.id) {
				throw "item not passed to CCH.Objects.Bucket";
			}

			var id = args.id,
				item = args.item;

			if (id) {
				item = me.getItemById(id);
			}

			if (item) {
				id = item.id;

				// Take the item out of my personal bucket array
				me.bucket.remove(function (item) {
					return item.id === id;
				});

				// Remove the item from the slide
				me.slide.remove(item);

				// Remove the item from the session
				CCH.session.removeItem(item);

				// Visually decrease the count
				me.decreaseCount();

				// Trigger the removal
				$(window).trigger('cch.bucket.card.removed', {
					id: id
				});
			}
		},
		removeAll: function () {
			me.bucket.each(function (item) {
				me.remove({
					item: item
				});
			});
		},
		bucket: me.bucket,
		getItems: function () {
			return me.bucket;
		},
		getItemById: function (id) {
			var item = null;
			if (id) {
				item = me.bucket.find(function (item) {
					return item.id === id;
				});
			}
			return item;
		},
		getCount: function () {
			return parseInt(me.bucketSVG.getCount(), 10);
		},
		setCount: function (args) {
			args = args || {};
			var count = parseInt(args.count, 10);

			me.countChanged();

			return count;
		},
		increaseCount: function () {
			var increaseCountFunction = function () {
				var count = me.getCount();

				count = count + 1;
				me.setCount({
					count: count
				});

				me.bucketSVG.bucketAdd();
			};
			
			if (me.bucketSVG) {
				increaseCountFunction();
			} else {
				$(document).on(me.bucketLoadEvent, increaseCountFunction);
			}
			
		},
		decreaseCount: function () {
			var decreaseCountFunction = function () {
				var count = me.getCount();

				if (count > 0) {
					count = count - 1;
				}

				me.setCount({
					count: count
				});

				me.bucketSVG.bucketSubtract();

				if (count === 0) {
					me.bucketSVG.bucketDump();
				}
			};
			
			if (me.bucketSVG) {
				decreaseCountFunction();
			} else {
				$(document).on(me.bucketLoadEvent, decreaseCountFunction);
			}
			
		},
		CLASS_NAME: 'CCH.Objects.Bucket'
	});

};