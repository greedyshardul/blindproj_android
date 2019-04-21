import * as functions from 'firebase-functions';
import * as firebaseHelper from 'firebase-functions-helper';
import * as admin from 'firebase-admin';
import * as express from 'express';
import * as bodyParser from "body-parser";
const app = express();
const main = express();
main.use('/api/v1', app);
main.use(bodyParser.json());
main.use(bodyParser.urlencoded({ extended: false }));
admin.initializeApp();
const usersCollection = 'users';
const db=admin.firestore();
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