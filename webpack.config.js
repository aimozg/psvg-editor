var webpack = require('webpack');
var path = require('path');
var node_dir = path.join(__dirname,'node_modules');
module.exports = {
    entry: {
        bundle:"./ts/story.ts", 
        svgedit:"./ts/svgedit.ts",
        vendor:[
            "jquery",
            "jstree",
            "underscore"
        ]
    },
    output: {
        path: "js",
        filename: "[name].js",
        libraryTarget: "var", 
        library: "[name]"
    },
    devtool: "#source-map",
    resolve: {
        modules: [ path.normalize(__dirname) ],
        extensions: ['.webpack.js', '.web.js', '.ts', '.js'],
        alias: {
            "jquery": path.join(node_dir,'jquery/dist/jquery.min.js'),
            "tinycolor2": path.join(node_dir,'tinycolor2/dist/tinycolor-min.js'),
            "underscore": path.join(node_dir,'underscore/underscore-min.js'),
            "jstree-css": path.join(node_dir,'jstree/dist/themes/default/style.css'),
            "jstree": path.join(node_dir,'jstree/dist/jstree.js'),
            "kotlinjs": path.join(__dirname, 'js/production/tfgame/tfgame.js'),
            "kotlin": path.join(__dirname, 'js/production/tfgame/kotlin.js'),
            "dom": path.join(__dirname, 'ts/dom.ts'),
            "svg": path.join(__dirname, 'ts/svg.ts')
        }
    },
    plugins: [
      /*new webpack.ProvidePlugin({
        $: "jquery"
      })*/
    ],
    module: {
        rules: [/*{
            test: /\.ts$/,
            use: 'uglify'
        },/**/{
            test: /\.css|\.scss$/,
            use: [
                'style-loader',
                'css-loader',
                'sass-loader'
            ]
        },{
            test: /\.ts$/, 
            use: 'ts-loader'
        },{
            test: /jquery\.min\.js|jstree\.js|underscore(-min)?\.js$/,
            use: 'script-loader'
        }, {
            test: /\.png|\.gif$/,
            use: 'url-loader?limit=100000'
        }, {
            test: /\.jpg$/,
            use: 'file-loader'
        }]
    }
};