private fun fetchUserEditNicknameAndSavePost(userId: String?, userEmail: String?, title: String, content: String, isPrivate: Boolean, date: String?, location: String?) {
    val userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId ?: "")
    userDocRef.get()
        .addOnSuccessListener { document ->
            val editNickname = document.getString("editnickname")
            if (selectedImageUri != null) {
                val storageReference = FirebaseStorage.getInstance().reference.child("images/${UUID.randomUUID()}")
                storageReference.putFile(selectedImageUri!!)
                    .addOnSuccessListener { taskSnapshot ->
                        taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            savePostToFirestore(title, content, isPrivate, imageUrl, userId, userEmail, editNickname, date, location)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                    }
            } else {
                savePostToFirestore(title, content, isPrivate, null, userId, userEmail, editNickname, date, location)
            }
        }
        .addOnFailureListener {
            Toast.makeText(this, "닉네임 가져오기 실패", Toast.LENGTH_SHORT).show()
        }