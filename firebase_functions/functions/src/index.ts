import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import * as firebaseHelper from 'firebase-functions-helper';
import * as express from 'express';
import * as bodyParser from "body-parser";
admin.initializeApp(functions.config().firebase);
const db = admin.firestore();
const app = express();
const main = express();
const usersCollection = 'users';
main.use('/api/v1', app);
main.use(bodyParser.json());
main.use(bodyParser.urlencoded({ extended: false }));
// webApi is your functions name, and you will pass main as 
// a parameter
export const webApi = functions.https.onRequest(main);
// View a contact
// View all users:just for demo
/* 
app.get('/phone', (req, res) => {
    firebaseHelper.firestore
        .backup(db, usersCollection)
        .then(data => res.status(200).send(data))
})
*/
// View a contact
app.get('/phone/:userEmail', (req, res) => {
    var userPhone;
    db.collection(usersCollection).where('Email', '==', req.params.userEmail).get()
          .then(function (querySnapshot) {
              // Once we get the results, begin a batch
              //var batch = db.batch();
              if(querySnapshot.empty) res.status(404).send(req.params.userEmail+" not found");

              querySnapshot.forEach(function (doc) {
                  // For each doc, add a delete operation to the batch
                  userPhone=doc.data().phone;
              });
              
          }).then(function () {
              
              console.log("found "+userPhone+" for "+req.params.userEmail);
              res.status(200).send(req.params.userEmail+":"+userPhone);
          }); 
          //res.status(404).send("not found");
          
})

//add and remove guardian. triggered by account creation/deletion
exports.addGuardian = functions.auth.user().onCreate((user) => {
    const email=user.email;
    //add name in firestore
    
    console.log("user "+email);
    var myDoc = {
        Email: email,
        phone: null

    };
    if(firebaseHelper.firestore
        .createNewDocument(db, usersCollection, myDoc)){
            console.log(email+" added successfuly");
            return 1;
        }
    return console.log("failed to write "+email);
    return 1;
  });
  exports.removeGuardian=functions.auth.user().onDelete((user)=>{
      //var docId;
      //var usersRef=db.collection(usersCollection);
      //var docQuery=usersRef.where("Email","==",user.email);
      db.collection("users").where('Email', '==', user.email).get()
          .then(function (querySnapshot) {
              // Once we get the results, begin a batch
              var batch = db.batch();

              querySnapshot.forEach(function (doc) {
                  // For each doc, add a delete operation to the batch
                  batch.delete(doc.ref);
              });

              // Commit the batch
              return batch.commit();
          }).then(function () {
              console.log("delete successful");
              return 1;
              // Delete completed!
              // ...
          }); 
          console.log("delete fail");
          return 0;
  });