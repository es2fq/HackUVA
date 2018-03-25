var express = require('express');
var fs = require('fs');
var bodyParser = require('body-parser');


var app = express();

// Set template engine 
var handlebars = require('express-handlebars').create({
    defaultLayout: 'main'
});
app.engine('handlebars', handlebars.engine);
app.set('view engine', 'handlebars');

// Set up file upload
var fileUpload = require('express-fileupload');
app.use(fileUpload());

// Set up body parsing
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
    extended:true
}));

app.set('port', process.env.PORT || 3000);
app.use(express.static(__dirname + '/public'));

app.get('/', function(req, res){
    var db = JSON.parse(fs.readFileSync('db.json'));
    res.render('index', {"items": db.items});
});

app.get('/upload', function(req, res){
    res.render('upload', {});
});

app.post('/upload', function(req, res){
    if(!req.files)
        return res.status(400).send('No files were uploaded.');
    var db = JSON.parse(fs.readFileSync('db.json'));
    var num = db.items.length;
    var filename = "public/midi/midi"+num.toString()+".midi";
    let midifile = req.files.midifile;
    
    midifile.mv(filename, function(err){
        if(err)
            return res.status(400).send(err);
        db.items.push({
            "name": req.body.name,
            "desc": req.body.desc,
            "filename": "midi"+num.toString()+".midi",
        });
        fs.writeFile('db.json', JSON.stringify(db), 'utf8', (err));
        res.send('File uploaded!');
    });
});

app.use(function(req, res){
    res.type('text/plain');
    res.status(404);
    res.send('404 - Not Found');
});

app.listen(app.get('port'), function(){
    console.log('Express server started on http://localhost:' + app.get('port') + '; press Ctrl-C to terminate...');
});