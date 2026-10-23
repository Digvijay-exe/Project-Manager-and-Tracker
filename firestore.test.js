const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const DATABASE_ID = "ai-studio-android-pipos-df021302-59ca-48bd-b729-c9611fa15178";
let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: "demo-no-project",
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085
    }
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("unauthenticated user cannot read or write projects", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore(DATABASE_ID);
  const projRef = unauthDb.doc("users/user123/projects/proj1");

  await assertFails(projRef.get());
  await assertFails(projRef.set({
    id: "proj1",
    name: "TerrainLens",
    status: "ACTIVE",
    userId: "user123",
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

test("authenticated user can create and read their own project", async () => {
  const authDb = testEnv.authenticatedContext("user123").firestore(DATABASE_ID);
  const projRef = authDb.doc("users/user123/projects/proj1");

  await assertSucceeds(projRef.set({
    id: "proj1",
    name: "TerrainLens",
    status: "ACTIVE",
    userId: "user123",
    createdAt: new Date(),
    updatedAt: new Date()
  }));

  await assertSucceeds(projRef.get());
});

test("user cannot read or write another user's project", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore(DATABASE_ID);
  const bobRef = aliceDb.doc("users/bob/projects/bobProj");

  await assertFails(bobRef.get());
  await assertFails(bobRef.set({
    id: "bobProj",
    name: "Secret Operation",
    status: "ACTIVE",
    userId: "bob",
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});
