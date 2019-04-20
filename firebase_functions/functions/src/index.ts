import * as functions from 'firebase-functions';
import * as firebaseHelper from 'firebase-functions-helper';
import * as admin from 'firebase-admin';
admin.initializeApp();
exports.addGuardian = functions.auth.user().onCreate((user) => {
    const name=user.displayName;
    //add name in firestore
    const usersCollection = 'users';
    const db=admin.firestore();
    firebaseHelper.firestore
        .createNewDocument(db, usersCollection, "mike");
  });