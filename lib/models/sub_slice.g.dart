// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sub_slice.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_SubSlice _$SubSliceFromJson(Map<String, dynamic> json) => _SubSlice(
  brand: json['brand'] == null
      ? null
      : Brand.fromJson(json['brand'] as Map<String, dynamic>),
  name: json['name'] as String,
  amount: (json['amount'] as num).toDouble(),
  color: (json['color'] as num).toInt(),
  startDate: DateTime.parse(json['startDate'] as String),
  frequency:
      $enumDecodeNullable(_$FrequencyEnumMap, json['frequency']) ??
      Frequency.monthly,
  category: json['category'] as String?,
  reminderMode:
      $enumDecodeNullable(_$ReminderModeEnumMap, json['reminderMode']) ??
      ReminderMode.both,
  cardLastFour: json['cardLastFour'] as String?,
);

Map<String, dynamic> _$SubSliceToJson(_SubSlice instance) => <String, dynamic>{
  'brand': instance.brand,
  'name': instance.name,
  'amount': instance.amount,
  'color': instance.color,
  'startDate': instance.startDate.toIso8601String(),
  'frequency': _$FrequencyEnumMap[instance.frequency]!,
  'category': instance.category,
  'reminderMode': _$ReminderModeEnumMap[instance.reminderMode]!,
  'cardLastFour': instance.cardLastFour,
};

const _$FrequencyEnumMap = {
  Frequency.daily: 'daily',
  Frequency.weekly: 'weekly',
  Frequency.monthly: 'monthly',
  Frequency.yearly: 'yearly',
};

const _$ReminderModeEnumMap = {
  ReminderMode.none: 'none',
  ReminderMode.onDay: 'onDay',
  ReminderMode.dayBefore: 'dayBefore',
  ReminderMode.both: 'both',
};
