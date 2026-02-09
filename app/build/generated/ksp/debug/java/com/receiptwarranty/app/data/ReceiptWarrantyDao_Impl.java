package com.receiptwarranty.app.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ReceiptWarrantyDao_Impl implements ReceiptWarrantyDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ReceiptWarranty> __insertionAdapterOfReceiptWarranty;

  private final EntityDeletionOrUpdateAdapter<ReceiptWarranty> __deletionAdapterOfReceiptWarranty;

  private final EntityDeletionOrUpdateAdapter<ReceiptWarranty> __updateAdapterOfReceiptWarranty;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public ReceiptWarrantyDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReceiptWarranty = new EntityInsertionAdapter<ReceiptWarranty>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `receipt_warranty` (`id`,`type`,`title`,`company`,`category`,`imageUri`,`purchaseDate`,`warrantyExpiryDate`,`reminderDays`,`notes`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReceiptWarranty entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, __ReceiptType_enumToString(entity.getType()));
        statement.bindString(3, entity.getTitle());
        statement.bindString(4, entity.getCompany());
        if (entity.getCategory() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getCategory());
        }
        if (entity.getImageUri() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getImageUri());
        }
        if (entity.getPurchaseDate() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getPurchaseDate());
        }
        if (entity.getWarrantyExpiryDate() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getWarrantyExpiryDate());
        }
        if (entity.getReminderDays() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, __ReminderDays_enumToString(entity.getReminderDays()));
        }
        if (entity.getNotes() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getNotes());
        }
        statement.bindLong(11, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfReceiptWarranty = new EntityDeletionOrUpdateAdapter<ReceiptWarranty>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `receipt_warranty` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReceiptWarranty entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfReceiptWarranty = new EntityDeletionOrUpdateAdapter<ReceiptWarranty>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `receipt_warranty` SET `id` = ?,`type` = ?,`title` = ?,`company` = ?,`category` = ?,`imageUri` = ?,`purchaseDate` = ?,`warrantyExpiryDate` = ?,`reminderDays` = ?,`notes` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReceiptWarranty entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, __ReceiptType_enumToString(entity.getType()));
        statement.bindString(3, entity.getTitle());
        statement.bindString(4, entity.getCompany());
        if (entity.getCategory() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getCategory());
        }
        if (entity.getImageUri() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getImageUri());
        }
        if (entity.getPurchaseDate() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getPurchaseDate());
        }
        if (entity.getWarrantyExpiryDate() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getWarrantyExpiryDate());
        }
        if (entity.getReminderDays() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, __ReminderDays_enumToString(entity.getReminderDays()));
        }
        if (entity.getNotes() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getNotes());
        }
        statement.bindLong(11, entity.getCreatedAt());
        statement.bindLong(12, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM receipt_warranty WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ReceiptWarranty item, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfReceiptWarranty.insertAndReturnId(item);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final ReceiptWarranty item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfReceiptWarranty.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ReceiptWarranty item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfReceiptWarranty.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ReceiptWarranty>> getAll() {
    final String _sql = "SELECT * FROM receipt_warranty ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"receipt_warranty"}, new Callable<List<ReceiptWarranty>>() {
      @Override
      @NonNull
      public List<ReceiptWarranty> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfCompany = CursorUtil.getColumnIndexOrThrow(_cursor, "company");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfPurchaseDate = CursorUtil.getColumnIndexOrThrow(_cursor, "purchaseDate");
          final int _cursorIndexOfWarrantyExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "warrantyExpiryDate");
          final int _cursorIndexOfReminderDays = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderDays");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<ReceiptWarranty> _result = new ArrayList<ReceiptWarranty>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReceiptWarranty _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final ReceiptType _tmpType;
            _tmpType = __ReceiptType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpCompany;
            _tmpCompany = _cursor.getString(_cursorIndexOfCompany);
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final Long _tmpPurchaseDate;
            if (_cursor.isNull(_cursorIndexOfPurchaseDate)) {
              _tmpPurchaseDate = null;
            } else {
              _tmpPurchaseDate = _cursor.getLong(_cursorIndexOfPurchaseDate);
            }
            final Long _tmpWarrantyExpiryDate;
            if (_cursor.isNull(_cursorIndexOfWarrantyExpiryDate)) {
              _tmpWarrantyExpiryDate = null;
            } else {
              _tmpWarrantyExpiryDate = _cursor.getLong(_cursorIndexOfWarrantyExpiryDate);
            }
            final ReminderDays _tmpReminderDays;
            if (_cursor.isNull(_cursorIndexOfReminderDays)) {
              _tmpReminderDays = null;
            } else {
              _tmpReminderDays = __ReminderDays_stringToEnum(_cursor.getString(_cursorIndexOfReminderDays));
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ReceiptWarranty(_tmpId,_tmpType,_tmpTitle,_tmpCompany,_tmpCategory,_tmpImageUri,_tmpPurchaseDate,_tmpWarrantyExpiryDate,_tmpReminderDays,_tmpNotes,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<ReceiptWarranty> getById(final long id) {
    final String _sql = "SELECT * FROM receipt_warranty WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"receipt_warranty"}, new Callable<ReceiptWarranty>() {
      @Override
      @Nullable
      public ReceiptWarranty call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfCompany = CursorUtil.getColumnIndexOrThrow(_cursor, "company");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfPurchaseDate = CursorUtil.getColumnIndexOrThrow(_cursor, "purchaseDate");
          final int _cursorIndexOfWarrantyExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "warrantyExpiryDate");
          final int _cursorIndexOfReminderDays = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderDays");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final ReceiptWarranty _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final ReceiptType _tmpType;
            _tmpType = __ReceiptType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpCompany;
            _tmpCompany = _cursor.getString(_cursorIndexOfCompany);
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final Long _tmpPurchaseDate;
            if (_cursor.isNull(_cursorIndexOfPurchaseDate)) {
              _tmpPurchaseDate = null;
            } else {
              _tmpPurchaseDate = _cursor.getLong(_cursorIndexOfPurchaseDate);
            }
            final Long _tmpWarrantyExpiryDate;
            if (_cursor.isNull(_cursorIndexOfWarrantyExpiryDate)) {
              _tmpWarrantyExpiryDate = null;
            } else {
              _tmpWarrantyExpiryDate = _cursor.getLong(_cursorIndexOfWarrantyExpiryDate);
            }
            final ReminderDays _tmpReminderDays;
            if (_cursor.isNull(_cursorIndexOfReminderDays)) {
              _tmpReminderDays = null;
            } else {
              _tmpReminderDays = __ReminderDays_stringToEnum(_cursor.getString(_cursorIndexOfReminderDays));
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new ReceiptWarranty(_tmpId,_tmpType,_tmpTitle,_tmpCompany,_tmpCategory,_tmpImageUri,_tmpPurchaseDate,_tmpWarrantyExpiryDate,_tmpReminderDays,_tmpNotes,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<String>> getAllCompanies() {
    final String _sql = "SELECT DISTINCT company FROM receipt_warranty ORDER BY company ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"receipt_warranty"}, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            _item = _cursor.getString(0);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<String>> getAllCategories() {
    final String _sql = "SELECT DISTINCT category FROM receipt_warranty WHERE category IS NOT NULL ORDER BY category ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"receipt_warranty"}, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            _item = _cursor.getString(0);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ReceiptWarranty>> getByCategory(final String category) {
    final String _sql = "SELECT * FROM receipt_warranty WHERE category = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, category);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"receipt_warranty"}, new Callable<List<ReceiptWarranty>>() {
      @Override
      @NonNull
      public List<ReceiptWarranty> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfCompany = CursorUtil.getColumnIndexOrThrow(_cursor, "company");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfPurchaseDate = CursorUtil.getColumnIndexOrThrow(_cursor, "purchaseDate");
          final int _cursorIndexOfWarrantyExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "warrantyExpiryDate");
          final int _cursorIndexOfReminderDays = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderDays");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<ReceiptWarranty> _result = new ArrayList<ReceiptWarranty>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReceiptWarranty _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final ReceiptType _tmpType;
            _tmpType = __ReceiptType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpCompany;
            _tmpCompany = _cursor.getString(_cursorIndexOfCompany);
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final Long _tmpPurchaseDate;
            if (_cursor.isNull(_cursorIndexOfPurchaseDate)) {
              _tmpPurchaseDate = null;
            } else {
              _tmpPurchaseDate = _cursor.getLong(_cursorIndexOfPurchaseDate);
            }
            final Long _tmpWarrantyExpiryDate;
            if (_cursor.isNull(_cursorIndexOfWarrantyExpiryDate)) {
              _tmpWarrantyExpiryDate = null;
            } else {
              _tmpWarrantyExpiryDate = _cursor.getLong(_cursorIndexOfWarrantyExpiryDate);
            }
            final ReminderDays _tmpReminderDays;
            if (_cursor.isNull(_cursorIndexOfReminderDays)) {
              _tmpReminderDays = null;
            } else {
              _tmpReminderDays = __ReminderDays_stringToEnum(_cursor.getString(_cursorIndexOfReminderDays));
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ReceiptWarranty(_tmpId,_tmpType,_tmpTitle,_tmpCompany,_tmpCategory,_tmpImageUri,_tmpPurchaseDate,_tmpWarrantyExpiryDate,_tmpReminderDays,_tmpNotes,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<CategoryCount>> getCategoryStats() {
    final String _sql = "SELECT category, COUNT(*) as count FROM receipt_warranty WHERE category IS NOT NULL GROUP BY category ORDER BY count DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"receipt_warranty"}, new Callable<List<CategoryCount>>() {
      @Override
      @NonNull
      public List<CategoryCount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCategory = 0;
          final int _cursorIndexOfCount = 1;
          final List<CategoryCount> _result = new ArrayList<CategoryCount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CategoryCount _item;
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final int _tmpCount;
            _tmpCount = _cursor.getInt(_cursorIndexOfCount);
            _item = new CategoryCount(_tmpCategory,_tmpCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getReceiptCount() {
    final String _sql = "SELECT COUNT(*) FROM receipt_warranty WHERE type = 'RECEIPT'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"receipt_warranty"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getWarrantyCount() {
    final String _sql = "SELECT COUNT(*) FROM receipt_warranty WHERE type = 'WARRANTY'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"receipt_warranty"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getActiveWarrantyCount(final long now) {
    final String _sql = "SELECT COUNT(*) FROM receipt_warranty WHERE type = 'WARRANTY' AND warrantyExpiryDate > ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, now);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"receipt_warranty"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getExpiringSoonCount(final long now, final long thresholdMs) {
    final String _sql = "SELECT COUNT(*) FROM receipt_warranty WHERE type = 'WARRANTY' AND warrantyExpiryDate - ? <= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, now);
    _argIndex = 2;
    _statement.bindLong(_argIndex, thresholdMs);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"receipt_warranty"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getExpiredWarrantyCount(final long now) {
    final String _sql = "SELECT COUNT(*) FROM receipt_warranty WHERE type = 'WARRANTY' AND warrantyExpiryDate < ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, now);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"receipt_warranty"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ReceiptWarranty>> getExpiringSoonItems(final long now, final long thirtyDaysMs) {
    final String _sql = "\n"
            + "        SELECT * FROM receipt_warranty \n"
            + "        WHERE type = 'WARRANTY' \n"
            + "        AND warrantyExpiryDate IS NOT NULL \n"
            + "        AND warrantyExpiryDate > ? \n"
            + "        AND warrantyExpiryDate - ? <= ? \n"
            + "        ORDER BY warrantyExpiryDate ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, now);
    _argIndex = 2;
    _statement.bindLong(_argIndex, now);
    _argIndex = 3;
    _statement.bindLong(_argIndex, thirtyDaysMs);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"receipt_warranty"}, new Callable<List<ReceiptWarranty>>() {
      @Override
      @NonNull
      public List<ReceiptWarranty> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfCompany = CursorUtil.getColumnIndexOrThrow(_cursor, "company");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfPurchaseDate = CursorUtil.getColumnIndexOrThrow(_cursor, "purchaseDate");
          final int _cursorIndexOfWarrantyExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "warrantyExpiryDate");
          final int _cursorIndexOfReminderDays = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderDays");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<ReceiptWarranty> _result = new ArrayList<ReceiptWarranty>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReceiptWarranty _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final ReceiptType _tmpType;
            _tmpType = __ReceiptType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpCompany;
            _tmpCompany = _cursor.getString(_cursorIndexOfCompany);
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final Long _tmpPurchaseDate;
            if (_cursor.isNull(_cursorIndexOfPurchaseDate)) {
              _tmpPurchaseDate = null;
            } else {
              _tmpPurchaseDate = _cursor.getLong(_cursorIndexOfPurchaseDate);
            }
            final Long _tmpWarrantyExpiryDate;
            if (_cursor.isNull(_cursorIndexOfWarrantyExpiryDate)) {
              _tmpWarrantyExpiryDate = null;
            } else {
              _tmpWarrantyExpiryDate = _cursor.getLong(_cursorIndexOfWarrantyExpiryDate);
            }
            final ReminderDays _tmpReminderDays;
            if (_cursor.isNull(_cursorIndexOfReminderDays)) {
              _tmpReminderDays = null;
            } else {
              _tmpReminderDays = __ReminderDays_stringToEnum(_cursor.getString(_cursorIndexOfReminderDays));
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ReceiptWarranty(_tmpId,_tmpType,_tmpTitle,_tmpCompany,_tmpCategory,_tmpImageUri,_tmpPurchaseDate,_tmpWarrantyExpiryDate,_tmpReminderDays,_tmpNotes,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ReceiptWarranty>> getItemsWithReminders() {
    final String _sql = "\n"
            + "        SELECT * FROM receipt_warranty \n"
            + "        WHERE type = 'WARRANTY' \n"
            + "        AND reminderDays IS NOT NULL \n"
            + "        AND warrantyExpiryDate IS NOT NULL\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"receipt_warranty"}, new Callable<List<ReceiptWarranty>>() {
      @Override
      @NonNull
      public List<ReceiptWarranty> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfCompany = CursorUtil.getColumnIndexOrThrow(_cursor, "company");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfPurchaseDate = CursorUtil.getColumnIndexOrThrow(_cursor, "purchaseDate");
          final int _cursorIndexOfWarrantyExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "warrantyExpiryDate");
          final int _cursorIndexOfReminderDays = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderDays");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<ReceiptWarranty> _result = new ArrayList<ReceiptWarranty>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReceiptWarranty _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final ReceiptType _tmpType;
            _tmpType = __ReceiptType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpCompany;
            _tmpCompany = _cursor.getString(_cursorIndexOfCompany);
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final Long _tmpPurchaseDate;
            if (_cursor.isNull(_cursorIndexOfPurchaseDate)) {
              _tmpPurchaseDate = null;
            } else {
              _tmpPurchaseDate = _cursor.getLong(_cursorIndexOfPurchaseDate);
            }
            final Long _tmpWarrantyExpiryDate;
            if (_cursor.isNull(_cursorIndexOfWarrantyExpiryDate)) {
              _tmpWarrantyExpiryDate = null;
            } else {
              _tmpWarrantyExpiryDate = _cursor.getLong(_cursorIndexOfWarrantyExpiryDate);
            }
            final ReminderDays _tmpReminderDays;
            if (_cursor.isNull(_cursorIndexOfReminderDays)) {
              _tmpReminderDays = null;
            } else {
              _tmpReminderDays = __ReminderDays_stringToEnum(_cursor.getString(_cursorIndexOfReminderDays));
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ReceiptWarranty(_tmpId,_tmpType,_tmpTitle,_tmpCompany,_tmpCategory,_tmpImageUri,_tmpPurchaseDate,_tmpWarrantyExpiryDate,_tmpReminderDays,_tmpNotes,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllItemsForExport(
      final Continuation<? super List<ReceiptWarranty>> $completion) {
    final String _sql = "SELECT * FROM receipt_warranty ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ReceiptWarranty>>() {
      @Override
      @NonNull
      public List<ReceiptWarranty> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfCompany = CursorUtil.getColumnIndexOrThrow(_cursor, "company");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfPurchaseDate = CursorUtil.getColumnIndexOrThrow(_cursor, "purchaseDate");
          final int _cursorIndexOfWarrantyExpiryDate = CursorUtil.getColumnIndexOrThrow(_cursor, "warrantyExpiryDate");
          final int _cursorIndexOfReminderDays = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderDays");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<ReceiptWarranty> _result = new ArrayList<ReceiptWarranty>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReceiptWarranty _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final ReceiptType _tmpType;
            _tmpType = __ReceiptType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpCompany;
            _tmpCompany = _cursor.getString(_cursorIndexOfCompany);
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final Long _tmpPurchaseDate;
            if (_cursor.isNull(_cursorIndexOfPurchaseDate)) {
              _tmpPurchaseDate = null;
            } else {
              _tmpPurchaseDate = _cursor.getLong(_cursorIndexOfPurchaseDate);
            }
            final Long _tmpWarrantyExpiryDate;
            if (_cursor.isNull(_cursorIndexOfWarrantyExpiryDate)) {
              _tmpWarrantyExpiryDate = null;
            } else {
              _tmpWarrantyExpiryDate = _cursor.getLong(_cursorIndexOfWarrantyExpiryDate);
            }
            final ReminderDays _tmpReminderDays;
            if (_cursor.isNull(_cursorIndexOfReminderDays)) {
              _tmpReminderDays = null;
            } else {
              _tmpReminderDays = __ReminderDays_stringToEnum(_cursor.getString(_cursorIndexOfReminderDays));
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ReceiptWarranty(_tmpId,_tmpType,_tmpTitle,_tmpCompany,_tmpCategory,_tmpImageUri,_tmpPurchaseDate,_tmpWarrantyExpiryDate,_tmpReminderDays,_tmpNotes,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __ReceiptType_enumToString(@NonNull final ReceiptType _value) {
    switch (_value) {
      case RECEIPT: return "RECEIPT";
      case WARRANTY: return "WARRANTY";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private String __ReminderDays_enumToString(@NonNull final ReminderDays _value) {
    switch (_value) {
      case ONE_DAY: return "ONE_DAY";
      case THREE_DAYS: return "THREE_DAYS";
      case FIVE_DAYS: return "FIVE_DAYS";
      case ONE_WEEK: return "ONE_WEEK";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private ReceiptType __ReceiptType_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "RECEIPT": return ReceiptType.RECEIPT;
      case "WARRANTY": return ReceiptType.WARRANTY;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }

  private ReminderDays __ReminderDays_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "ONE_DAY": return ReminderDays.ONE_DAY;
      case "THREE_DAYS": return ReminderDays.THREE_DAYS;
      case "FIVE_DAYS": return ReminderDays.FIVE_DAYS;
      case "ONE_WEEK": return ReminderDays.ONE_WEEK;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
