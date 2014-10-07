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

    // model with basic feed information
    var FeedModel = Backbone.Model.extend();

    // template for basic information
    var feedTemplate = _.template(require('./feed.html'));

    // view for a header with basic information about a feed
    var FeedView = Backbone.View.extend({
	render: function () {
	    this.$el.html(feedTemplate(this.model.attributes));
	    return this;
	}
    });

    // represents a type of error, with singular and plural human-readable forms
    var TypeModel = Backbone.Model.extend();
    
    var InvalidValueListView = Backbone.View.extend({
	tagName: 'div',
	className: 'panel panel-default',

	render: function () {
	    this.$el.html(invalidValuesListTemplate({type: this.model.attributes.pl, errorCount: this.collection.length}));

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

    // load the json and, when both it and the DOM are loaded, render it
    var routes, stops, trips, shapes;

    $.ajax({
	// TODO: hardwired is bad
	url: '/out.json',
	dataType: 'json',
	success: function (data) {
	    var feed = new FeedModel({
		agencies: data.agencies,
		agencyCount: data.agencyCount,
		tripCount: data.tripCount,
		routeCount: data.routeCount,
		startDate: new Date(data.startDate),
		endDate: new Date(data.endDate),
		stopCount: data.stopCount,
		stopTimesCount: data.stopTimesCount
	    });

	    new FeedView({model: feed, el: '#header'}).render();

	    // TODO: check for total load failure by OBA, i.e. missing required fields and so on
	    routes = new InvalidValueColl(data.routes.invalidValues);
	    stops = new InvalidValueColl(data.stops.invalidValues);
	    trips = new InvalidValueColl(data.trips.invalidValues);
	    shapes = new InvalidValueColl(data.shapes.invalidValues);

	    // create the panels and populate them
	    new InvalidValueListView({collection: routes, model: new TypeModel({sing: 'Route', pl: 'Routes'})}).render().$el.appendTo($('#content'));
	    new InvalidValueListView({collection: trips, model: new TypeModel({sing: 'Trip', pl: 'Trips'})}).render().$el.appendTo($('#content'));
	    new InvalidValueListView({collection: stops, model: new TypeModel({sing: 'Stop', pl: 'Stops'})}).render().$el.appendTo($('#content'));
	    new InvalidValueListView({collection: shapes, model: new TypeModel({sing: 'Shape', pl: 'Shapes'})}).render().$el.appendTo($('#content'));
	},
	error: function () { console.log('oops'); },
    });
});
    
