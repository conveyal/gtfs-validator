    // Gulp Dependencies
    var gulp = require('gulp');
    var gp_rename = require('gulp-rename');
    var browserify = require('browserify');
    var source = require('vinyl-source-stream');
    var cleanCSS = require("gulp-clean-css");
    var underscorify = require("node-underscorify");
    var gutil = require("gulp-util");

    gulp.task('build-js', function() {
        return browserify('./validation.js')
            .transform(underscorify.transform())
            .bundle()
            .on('error', function(err) {
                gutil.log(err);
            })
            .pipe(source('build.js'))
            .pipe(gulp.dest('build'))
    });

    gulp.task('build-css', function() {
        return gulp.src('./style.css')
            .pipe(gp_rename('build.css'))
            .pipe(cleanCSS())
            .pipe(gulp.dest('build'));
        });

    gulp.task('default', ['build-js', 'build-css'], function(){});
