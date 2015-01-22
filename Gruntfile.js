'use strict';

// # Globbing
// for performance reasons we're only matching one level down:
// 'test/spec/{,*/}*.js'
// use this if you want to recursively match all subfolders:
// 'test/spec/**/*.js'


//var client = 'src/main/client';
var client = 'src/main/webapp/assets';


module.exports = function (grunt) {
	var localConfig;
	try {
		localConfig = require('./server/config/local.env');
	} catch(e) {
		localConfig = {};
	}

	// Load grunt tasks automatically, when needed
	require('jit-grunt')(grunt, {
		express: 'grunt-express-server',
		useminPrepare: 'grunt-usemin',
		ngtemplates: 'grunt-angular-templates',
		cdnify: 'grunt-google-cdn',
		protractor: 'grunt-protractor-runner',
		injector: 'grunt-asset-injector',
		buildcontrol: 'grunt-build-control'
	});

	// Time how long tasks take. Can help when optimizing build times
	require('time-grunt')(grunt);

	// load environment variables
	grunt.loadNpmTasks('grunt-env');

	// Define the configuration for all the tasks
	grunt.initConfig({
		// Project settings
		yeoman: {
			// configurable paths
			client: require('./bower.json').appPath || client,
			dist: 'target/client'
		},

		watch: {
			injectJS: {
				files: [
				  '<%= yeoman.client %>/modules/**/*.js',
				  '!<%= yeoman.client %>/modules/**/*.spec.js',
				  '!<%= yeoman.client %>/modules/**/*.mock.js',
				  '!<%= yeoman.client %>/modules/app.js'],
				tasks: ['injector:scripts']
			},
			injectCss: {
			files: [
			  '<%= yeoman.client %>/modules/**/*.css'
			],
			tasks: ['injector:css']
			},
			injectSass: {
				files: [
			 	 '<%= yeoman.client %>/modules/**/*.{scss,sass}'],
				tasks: ['injector:sass']
			},
			sass: {
				files: [
			  		'<%= yeoman.client %>/modules/**/*.{scss,sass}'
			  	],
				tasks: ['sass', 'autoprefixer']
			},
			gruntfile: {
				files: ['Gruntfile.js']
			}
		},

		// Make sure code styles are up to par and there are no obvious mistakes
		jshint: {
			options: {
				jshintrc: '.jshintrc',
				reporter: require('jshint-stylish')
	  		},
	  		client: [
				'Gruntfile.js',
				'<%= yeoman.client %>/{,*/}*.js'
	  		],
			server: {
				src: [
					'server/{,*/}*.js'
				]
			}
		},

		// open the browser in the given URL
		open: {
			server: {
				url: 'http://localhost:8080/sitetb/assets/public.html'
			}
		},

		// Empties folders to start fresh
		clean: {
			dist: {
				files: [{
		  		dot: true,
				src: [
					'target/.tmp',
					'<%= yeoman.dist %>/*',
					'!<%= yeoman.dist %>/.git*'
					]
				}]
			},
			server: 'target/.tmp'
		},


		injector: {
			options: {

			},
			// Inject application script files into index.html (doesn't include bower)
			scripts: {
				options: {
				  transform: function(filePath) {
				    filePath = filePath.replace('/' + client + '/', '');
				    return '<script src="' + filePath + '"></script>';
				  },
				  starttag: '<!-- injector:js -->',
				  endtag: '<!-- endinjector -->'
				},
				files: {
				  '<%= yeoman.client %>/app.template.html': [
				      ['{target/.tmp,<%= yeoman.client %>}/modules/app/**/*.js',
				       '<%= yeoman.client %>/modules/commons/**/*.js',
				       '<%= yeoman.client %>/modules/directives/**/*.js',
				       '!{target/.tmp,<%= yeoman.client %>}/modules/app/app.js',
				       '!{target/.tmp,<%= yeoman.client %>}/modules/app/**/*.spec.js',
				       '!{target/.tmp,<%= yeoman.client %>}/modules/app/**/*.mock.js']
				    ],
				  '<%= yeoman.client %>/public.template.html': [
				      ['{target/.tmp,<%= yeoman.client %>}/modules/public/**/*.js',
				       '<%= yeoman.client %>/modules/commons/**/*.js',
				       '<%= yeoman.client %>/modules/directives/**/*.js',
				       '!{target/.tmp,<%= yeoman.client %>}/modules/public/app.js',
				       '!{target/.tmp,<%= yeoman.client %>}/modules/public/**/*.spec.js',
				       '!{target/.tmp,<%= yeoman.client %>}/modules/public/**/*.mock.js']
				    ]
				}
			},

			// Inject component scss into app.scss
			sass: {
				options: {
				  transform: function(filePath) {
				    filePath = filePath.replace('/' + client + '/modules/public/', '');
				    filePath = filePath.replace('/' + client + '/modules/app/', '');
				    return '@import \'' + filePath + '\';';
				  },
				  starttag: '// injector',
				  endtag: '// endinjector'
				},
				files: {
				  '<%= yeoman.client %>/modules/app/app.scss': [
				    '<%= yeoman.client %>/modules/app/**/*.{scss,sass}',
				    '!<%= yeoman.client %>/modules/app/app.{scss,sass}'
				  ],
				  '<%= yeoman.client %>/modules/public/app.scss': [
				    '<%= yeoman.client %>/modules/public/**/*.{scss,sass}',
				    '!<%= yeoman.client %>/modules/public/app.{scss,sass}'
				  ]
				}
			},

			// Inject component css into index.html
			css: {
				options: {
				  transform: function(filePath) {
				    filePath = filePath.replace('<%= yeoman.client %>/', '');
				    filePath = filePath.replace('/target/.tmp/', '');
				    return '<link rel="stylesheet" href="' + filePath + '">';
				  },
				  starttag: '<!-- injector:css -->',
				  endtag: '<!-- endinjector -->'
				},
				files: {
				  '<%= yeoman.client %>/public.template.html': [
				    '<%= yeoman.client %>/{app,components}/**/*.css'
				  ]
				}
			}
		},


		// Run some tasks in parallel to speed up the build process
		concurrent: {
			server: [
		    	'sass'
			],
			test: [
		    	'sass'
			],
			debug: {
		    	tasks: [
		    	'nodemon',
		    	'node-inspector'
		    	],
		    	options: {
		    		logConcurrentOutput: true
		    	}
		  	},
		  	dist: [
		    	'sass'
//		    	'imagemin',
//		    	'svgmin'
		  	]
		},

		// environment variables
		env: {
			test: {
				NODE_ENV: 'test'
			},
			prod: {
		    	NODE_ENV: 'production'
			},
			all: localConfig
		},


		// Compiles Sass to CSS
		sass: {
			pub: {
				options: {
					loadPath: [
					    '<%= yeoman.client %>/bower_components',
					    '<%= yeoman.client %>/modules/public'
					],
					compass: false
				},
				files: {
					'<%= yeoman.client %>/styles/public.css' : '<%= yeoman.client %>/modules/public/app.scss'
				}
			},
			app: {
				options: {
					loadPath: [
					    '<%= yeoman.client %>/bower_components',
					    '<%= yeoman.client %>/modules/app'
					],
					compass: false
				},
				files: {
					'<%= yeoman.client %>/styles/app.css' : '<%= yeoman.client %>/modules/app/app.scss'
				}
			}
		},

		// Add vendor prefixed styles
		autoprefixer: {
		  options: {
		    browsers: ['last 1 version']
		  },
		  dist: {
		    files: [{
		      expand: true,
		      cwd: 'target/.tmp/',
		      src: '{,*/}*.css',
		      dest: 'target/.tmp/'
		    }]
		  }
		},

		// configure mocha test
		mochaTest: {
		  server: {
		    options: {
//		      reporter: 'list',
		      clearRequireCache: true
		    },
		    src: ['server/test/**/*.js']
		  }
		},
	
		// Allow the use of non-minsafe AngularJS files. Automatically makes it
		// minsafe compatible so Uglify does not destroy the ng references
		ngAnnotate: {
		  dist: {
		    files: [{
		      expand: true,
		      cwd: 'target/.tmp/concat',
		      src: '*/**.js',
		      dest: 'target/.tmp/concat'
		    }]
		  }
		},

	    // Reads HTML for usemin blocks to enable smart builds that automatically
	    // concat, minify and revision files. Creates configurations in memory so
	    // additional tasks can operate on them
	    useminPrepare: {
	      html: ['<%= yeoman.dist %>/public.template.html', '<%= yeoman.dist %>/app.template.html'],
	      options: {
	        dest: '<%= yeoman.dist %>',
            staging: 'target/.tmp'
	      }
	    },
	
	    // Performs rewrites based on rev and the useminPrepare configuration
	    usemin: {
	      html: ['<%= yeoman.dist %>/{,*/}*.html'],
	      css: ['<%= yeoman.dist %>/{,*/}*.css'],
	      js: ['<%= yeoman.dist %>/{,*/}*.js'],
	      options: {
	        assetsDirs: [
	          '<%= yeoman.dist %>',
	          '<%= yeoman.dist %>/images'
	        ],
	        // This is so we update image references in our ng-templates
	        patterns: {
	          js: [
	            [/(assets\/images\/.*?\.(?:gif|jpeg|jpg|png|webp|svg))/gm, 'Update the JS to reference our revved images']
	          ]
	        }
	      }
	    },
		
		// copy some files to the distribution folder
		copy: {
		  dist: {
		    files: [{
		      expand: true,
		      dot: true,
		      cwd: '<%= yeoman.client %>',
		      dest: '<%= yeoman.dist %>',
		      src: [
		        '*.{ico,png,txt}',
		        '.htaccess',
		        'bower_components/**/*',
		        'images/{,*/}*.{webp}',
		        'fonts/**/*',
                  'styles/**/*',
                'modules/**/*.html',
		        'index.html',
		        'public.template.html',
		        'app.template.html'
		      ]
		    }, {
		      expand: true,
		      cwd: 'target/.tmp/images',
		      dest: '<%= yeoman.dist %>/client/assets/images',
		      src: ['generated/*']
		    }]
		  }
		},

		// Renames files for browser caching purposes
		rev: {
		  dist: {
		    files: {
		      src: [
		        '<%= yeoman.dist %>/{,*/}*.js',
		        '<%= yeoman.dist %>/{,*/}*.css',
		        '<%= yeoman.dist %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
		      ]
		    }
		  }
		},

		// minify html pages
		htmlmin: {
			dist: {
				options: {
					removeComments: true,
		        	removeCommentsFromCDATA: true,
		        	useShortDocType: true,
			        collapseBooleanAttributes: true,
		        	removeRedundantAttributes: true,
					collapseWhiteSpace: true,
					minifyJS: {hoist_vars:true}
				},
				files: [{
        		  expand: true,
		          cwd: '<%= yeoman.dist %>/client',
		          src: ['*.html', '**/*.html'],
		          dest: '<%= yeoman.dist %>/client'
		        }]
			}
		},

		eol: {
			dist: {
				options: {
					eol: 'lf',
					replace: true
				},
				files: [{
					'src': ['<%= yeoman.dist %>/public.template.html', '<%= yeoman.dist %>/app.template.html']
				}]
			}
		}
});


  /* Execution of the application in development mode */
  grunt.registerTask('run', [
	'jshint',
	'injector',
//	'i18n_template:keys', 'i18n_template:json', 'i18n_template:msgs', 'i18n_template:templates',
//	'express:dev',
	'open',
	'watch'
  ]);


  grunt.registerTask('debug', [
	'concurrent:debug'
  ]);

  grunt.registerTask('test', [
	'clean:server',
	'jshint',
	'injector',
	'i18n_template:json', 'i18n_template:msgs', 'i18n_template:templates',
	'concurrent:test',
	'autoprefixer',
	'mochaTest:server'
//	'connect:test',
//	'karma'
  ]);

  grunt.registerTask('build', [
	'clean:dist',
    'injector:sass',
    'concurrent:dist',
    'injector',
    'copy:dist',
    'eol',
	'useminPrepare',
    'autoprefixer',
	'concat',
	'ngAnnotate',
	'cssmin',
	'uglify',
	'rev',
	'usemin',
	'htmlmin'
  ]);

  grunt.registerTask('default', [
	'newer:jshint',
//	'test',
	'build'
  ]);
};
