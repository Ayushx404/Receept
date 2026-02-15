# Deletion Sync Test Plan

## Test Scenarios

### 1. Basic Deletion Flow
**Steps:**
1. Create a new receipt/warranty item
2. Let it sync to cloud
3. Delete the item from the app
4. Check if item is deleted from:
   - Local database
   - Firestore (cloud)
   - Google Drive (if has image)
5. Wait 5 minutes
6. Check if item is NOT re-added during sync

**Expected Result:** Item should be deleted from all locations and not re-added

### 2. Deletion with Offline Mode
**Steps:**
1. Create a new item
2. Let it sync
3. Turn off internet
4. Delete the item locally
5. Wait 1 minute
6. Turn on internet
7. Trigger sync
8. Check if item is deleted from cloud

**Expected Result:** Deletion should be propagated to cloud when online

### 3. Deletion with Conflict Resolution
**Steps:**
1. Create an item on Device A
2. Sync to cloud
3. Modify the same item on Device B
4. Delete the item on Device A
5. Sync Device A
6. Sync Device B
7. Check deletion status

**Expected Result:** 
- Item should be deleted on both devices
- No re-adding should occur

### 4. Multiple Deletions
**Steps:**
1. Create 5 items
2. Sync them all
3. Delete 3 items
4. Sync
5. Check if only 2 items remain

**Expected Result:** Only the 2 non-deleted items should remain

### 5. Deletion with Image
**Steps:**
1. Create item with image
2. Sync (image uploaded to Drive)
3. Delete item
4. Check if:
   - Item deleted from Firestore
   - Image deleted from Drive
   - Local item deleted
   - Image file deleted locally

**Expected Result:** All references cleaned up

### 6. Cleanup of Old Deletion Records
**Steps:**
1. Simulate old deletion records (30+ days old)
2. Trigger sync
3. Check if old deletion records are cleaned up

**Expected Result:** Old deletion records removed from database

## Verification Steps

### Check Database
```sql
-- Check if deleted_items table exists
SELECT name FROM sqlite_master WHERE type='table' AND name='deleted_items';

-- Check deleted items count
SELECT COUNT(*) FROM deleted_items WHERE userId = 'your_user_id';

-- Check receipt_warranty table structure
PRAGMA table_info(receipt_warranty);
```

### Check Logs
Look for these log messages:
- "Tracked deletion for cloudId: ..."
- "Skipping deleted item: ..."
- "Cleaning up old deleted items"
- "Updated local item ... with newer cloud version"
- "Keeping local item ... (local version is newer)"

### Check Firestore
1. Go to Firebase Console
2. Check Firestore database
3. Verify deleted items are actually removed
4. Check updatedAt timestamps

## Rollback Plan

If issues occur:

1. **Immediate Rollback:**
   - Comment out deletion tracking code in `SyncStateManager.deleteItem()`
   - Remove deleted_items table from migration
   - Revert changes in `syncAll()` method

2. **Partial Rollback (keeping timestamp conflict resolution):**
   - Keep timestamp conflict resolution
   - Disable deletion tracking
   - Use original sync logic

3. **Emergency Fix:**
   - Clear `deleted_items` table if it gets corrupted
   - Add manual sync reset functionality

## Expected Behavior Summary

| Action | Local Result | Cloud Result | Drive Result | Sync Behavior |
|--------|--------------|--------------|--------------|---------------|
| Delete item | Item removed | Item removed | Image removed | Skipped in sync |
| Delete offline | Item removed | Pending deletion | Pending deletion | Queued, sync when online |
| Add new item | Item added | Item added | Image uploaded | Immediate sync |
| Update existing | Item updated | Item updated | Image re-uploaded | Immediate sync |
| Conflict | Newer timestamp wins | Newer timestamp wins | Newer timestamp wins | Timestamp comparison |

## Performance Considerations

1. **Database Size:** deleted_items table should stay small (max ~1000 entries)
2. **Sync Time:** Should be faster due to fewer items processed
3. **Memory Usage:** Minimal - only storing IDs and timestamps
4. **Network Traffic:** Reduced - no re-upload of deleted items

## Security Considerations

1. **User Isolation:** Each user's deletions are tracked separately
2. **Data Privacy:** Only cloud IDs are stored, no sensitive data
3. **Cleanup Policy:** Old deletion records automatically removed
4. **Offline Security:** Local deletions don't expose cloud data

## Implementation Notes

### Files Modified:
1. `DeletedItem.kt` - New entity for tracking deletions
2. `ReceiptWarranty.kt` - Added updatedAt field
3. `AppDatabase.kt` - Added migration 4→5
4. `ReceiptWarrantyDao.kt` - Added deletion tracking methods
5. `ReceiptWarrantyRepository.kt` - Added deletion tracking methods
6. `SyncStateManager.kt` - Modified sync logic and deleteItem
7. `ReceiptWarrantyViewModel.kt` - Modified deleteById and insertOrUpdate
8. `FirestoreModels.kt` - Updated to handle updatedAt
9. `AppContainer.kt` - Updated to pass repository to SyncStateManager

### Key Changes Summary:
- **Database v5:** Added deleted_items table and updatedAt column
- **Sync Logic:** Check for deleted items before inserting
- **Conflict Resolution:** Compare timestamps (newer wins)
- **Deletion Tracking:** Track deletions to prevent re-adding
- **Cleanup:** Automatic cleanup of old deletion records