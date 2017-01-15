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
        root: path.normalize(__dirname),
        extensions: ['', '.webpack.js', '.web.js', '.ts', '.js'],
        alias: {
            "jquery": path.join(node_dir,'jquery/dist/jquery.min.js'),
            "tinycolor2": path.join(node_dir,'tinycolor2/dist/tinycolor-min.js'),
            "underscore": path.join(node_dir,'underscore/underscore-min.js'),
            "jstree": path.join(__dirname,'libs/vakata-jstree/dist/jstree.js')
        }
    },
    plugins: [
      /*new webpack.ProvidePlugin({
        $: "jquery"
      })*/
    ],
    module: {
        loaders: [/*{
            test: /\.ts$/,
            loader: 'uglify'
        }/**/,{ 
            test: /\.ts$/, 
            loader: 'ts-loader' 
        },{
            test: /jquery\.min\.js|jstree\.js|underscore(-min)?\.js$/,
            loader: 'script-loader'
        }]
    }
};