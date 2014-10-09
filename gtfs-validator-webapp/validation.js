// validation.js: display the JSON from running the validator in a human-readable fashion

var $ = require('jquery');
var Backbone = require('backbone');
Backbone.$ = $;
var _ = require('underscore');

// framework for representing invalid values
$(document).ready(function () {
    var InvalidValue = Backbone.Model.extend({});
    var InvalidValueColl = Backbone.Collection.extend({
	model: InvalidValue
    });

    // template for showing invalid values
    var invalidValuesTemplate = _.template('<td><%- problemType %></td><td><%- affectedEntity %></td><td><%- affectedField %></td><td><%- problemDescription %></td>');

    // template for a table/list of invalid values
    var invalidValuesListTemplate = _.template(require('./list.html'));
    
    // view for invalid values
    var InvalidValueView = Backbone.View.extend({
	tagName: 'tr',
	className: 'invalid-value',
	render: function () {
	    this.$el.html(invalidValuesTemplate(this.model.attributes));
	    return this;
	}
    });

    // model with basic feed information as well as invalid value information specific to a feed
    var FeedModel = Backbone.Model.extend();

    // template for basic information
    var feedTemplate = _.template(require('./feed.html'));

    // view for a header with basic information about a feed
    var FeedView = Backbone.View.extend({
	tagName: 'div',
	className: 'tab-pane',
	id: function () { return 'feed-' + this.model.attributes.index },
	render: function () {
	    this.$el.html(feedTemplate(this.model.attributes));

	    // append the invalid value information
	    // create the panels and populate them, but only if the load was successful (otherwise we have nothing to show)
	    if (this.model.attributes.loadStatus == 'SUCCESS') {
		var content = this.$('.error-panel');

		// the index is so that link hrefs remain unique
		new InvalidValueListView({collection: this.model.attributes.routes, model: new TypeModel({sing: 'Route', pl: 'Routes', index: this.model.attributes.index})}).render().$el.appendTo(content);
		new InvalidValueListView({collection: this.model.attributes.trips, model: new TypeModel({sing: 'Trip', pl: 'Trips', index: this.model.attributes.index})}).render().$el.appendTo(content);
		new InvalidValueListView({collection: this.model.attributes.stops, model: new TypeModel({sing: 'Stop', pl: 'Stops', index: this.model.attributes.index})}).render().$el.appendTo(content);
		new InvalidValueListView({collection: this.model.attributes.shapes, model: new TypeModel({sing: 'Shape', pl: 'Shapes', index: this.model.attributes.index})}).render().$el.appendTo(content);
	    }	
	    
	    return this;
	}
    });

    // represents a type of error, with singular and plural human-readable forms
    var TypeModel = Backbone.Model.extend();
    
    var InvalidValueListView = Backbone.View.extend({
	tagName: 'div',
	className: 'panel panel-default',

	render: function () {
	    this.$el.html(invalidValuesListTemplate({type: this.model.attributes.pl, errorCount: this.collection.length, index: this.model.attributes.index}));

	    var tbody = this.$('tbody');

	    // populate the table
	    this.collection.each(function (item) {
		new InvalidValueView({model: item}).render().$el.appendTo(tbody);
	    });

	    return this;
	},

	/** Pass in an InvalidValueColl to populate this list view */
	populate: function (coll) {
	    var tbody = this.$el.find('tbody');

	}
    });

    // represents an entire validation run
    var ValidationRunModel = Backbone.Model.extend();

    var validationRunTemplate = _.template(require('./validationrun.html'));
    var feedTabTemplate = _.template(require('./feedTab.html'));
    var feedListEntryTemplate = _.template(require('./feedList.html'));

    // displays an entire validation run
    var ValidationRunView = Backbone.View.extend({
	el: '#content',
	render: function () {
	    this.$el.html(validationRunTemplate(this.model.attributes));

	    // now attach the feed information
	    var content = this.$('.the-tabs');
	    var feedNav = this.$('.feed-nav');
	    var feedList = this.$('.feed-list');
	    this.collection.each(function (feed) {
		new FeedView({model: feed}).render().$el.appendTo(content);
		feedNav.append(feedTabTemplate(feed.attributes));
		feedList.append(feedListEntryTemplate(feed.attributes))
	    });

	    return this;
	}
    });

    // represents an application error
    var ErrorModel = Backbone.Model.extend({
	defaults: {
	    title: 'Application error',
	    message: ''
	}
    });
    var errorTemplate = _.template(require('./error.html'));
    var ErrorView = Backbone.View.extend({
	className: 'bg-danger error',

	render: function () {
	    this.$el.html(errorTemplate(this.model.attributes));
	    return this;
	}
    });	    

    // A collection of feed validation results
    var FeedColl = Backbone.Collection.extend();

    // figure out what file we're pulling from
    // TODO: malformed search string handling
    var params = {};
    // some browsers (I'm looking at you, Firefox) append a trailing slash after the query params
    if (location.search[location.search.length - 1] == '/')
	var search = location.search.slice(0, -1);
    else
	var search = location.search;
    var splitSearch = search.slice(1).split('&');
    for (var i = 0; i < splitSearch.length; i++) {
	
	if (!splitSearch[i].contains('=')) {
	    params[splitSearch[i]] = null;
	    continue;
	}
	
	var splitParam = splitSearch[i].split('=');
	params[splitParam[0]] = decodeURIComponent(splitParam[1]);
    }

    if (params['report'] == undefined) {
	new ErrorView({
	    model: new ErrorModel({title: 'No report specified', message: 'Please specify a report to view'})
	}).render().$el.appendTo('#content');

	return;
    }

    if (params.report.startsWith('//') || params.report.contains('://')) {
	new ErrorView({
	    model: new ErrorModel({title: 'Only local reports may be viewed', message: 'Please specify a local report to view'})
	}).render().$el.appendTo('#content');

	return;
    }    

    // load the json and, when both it and the DOM are loaded, render it
    var routes, stops, trips, shapes;

    $.ajax({
	url: params['report'],
	dataType: 'json',
	success: function (data) {
	    var run = new ValidationRunModel({
		name: data.name,
		date: new Date(data.date),
		feedCount: data.feedCount,
		loadCount: data.loadCount
	    });

	    var feeds = new FeedColl();

	    var nfeeds = data.results.length;
	    for (var i = 0; i < nfeeds; i++) {
		var feedData = data.results[i];
		
		var routes, trips, stops, shapes;
		if (feedData.loadStatus == 'SUCCESS') {
		    routes= new InvalidValueColl(feedData.routes.invalidValues);
		    stops= new InvalidValueColl(feedData.stops.invalidValues);
		    trips= new InvalidValueColl(feedData.trips.invalidValues);
		    shapes= new InvalidValueColl(feedData.shapes.invalidValues);
		}
		else {
		    routes = shapes = trips = stops = null;
		}

		feed = new FeedModel({
		    agencies: feedData.agencies,
		    agencyCount: feedData.agencyCount,
		    tripCount: feedData.tripCount,
		    routeCount: feedData.routeCount,
		    startDate: new Date(feedData.startDate),
		    endDate: new Date(feedData.endDate),
		    stopCount: feedData.stopCount,
		    stopTimesCount: feedData.stopTimesCount,
		    loadStatus: feedData.loadStatus,
		    feedFileName: feedData.feedFileName,
		    loadFailureReason: feedData.loadFailureReason,
		    
		    // just need a guaranteed-unique value attached to each feed for tabnav
		    index: i,
		    
		    routes: routes,
		    trips: trips,
		    stops: stops,
		    shapes: shapes,
		});

		feeds.add(feed);		
	    }

	    new ValidationRunView({model: run, collection: feeds}).render().$el.appendTo($('#content'));

	    // when we click on a link to a tab, go to that tab
	    // see http://stackoverflow.com/questions/15360112
	    $('.tab-jump').click(function (e) {
		var t = $('.feed-nav a[href="' + $(this).attr('href') + '"]');
		t.tab('show');
		e.preventDefault();
	    });
	},
	error: function () {
	    new ErrorView({
		model: new ErrorModel({title: 'Report could not be loaded', message: 'There was an error loading the report ' + params.report + '. Does it exist?'})
	    }).render().$el.appendTo('#content');
	},
    });
});
    
