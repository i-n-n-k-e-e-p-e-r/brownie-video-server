### Version 1.0.5:
##### Fiches
- Added viewed status for every file and ability to mark 
  as un viewed for every user group
- New folder creation added
- Rename file / folder added
- Ability to move files added

##### Fixes
- Race condition fixed on subfolder deletion in 'uploads' root
- Selection grid bug fixed (refresh all, then rename file)
- Check for encoding added on single file deletion
- Some new unit tests

### Version 1.0.4:
##### Fix
- Clearing temporary files factory removed from upload pipeline 
  (it causes falls on the detached UI)
- Memory leak (on hdd/ssd) fixed in uploads dialog
- Enhanced temporary files management after uploading finished
- Unit tests develop started

### Version 1.0.3:
##### Fix
- Audio encodes in 256kbs
- 30fps removed from decoder config hardcode (now same as source)
- Enhanced disc space management
- Uploads process stability fixes (process for every single file, 
  not for all in parallel)

### Version 1.0.2:
##### Fix
- Fixed video controls out of view in some cases
- Fixed encoding video files with wrong MIME types

### Version 1.0.1: 
##### Fiches
- Combo box with created folders added to upload dialog
- Exit button added
- Disables download button while encoding video
- Method of getting MIME type changed
- Main layout of application and file viewer dialog changed to AppLayout
- Download button added in file viewer dialog
- Fav and logo icons added
- Other cosmetic enhancements and changes
##### Fix
- Fixed issue with cached file buffer while uploading
- Fixed temporary deletion after processing to uploads dir
