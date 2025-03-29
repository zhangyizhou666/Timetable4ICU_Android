#!/usr/bin/env python3
# Script to convert schedule_time in CSV file to ICS format

import re
import csv
import os

# Path to the CSV file
input_file = os.path.join("app", "src", "main", "assets", "courses_2025_all_terms.csv")
output_file = os.path.join("app", "src", "main", "assets", "courses_2025_all_terms_ics.csv")

# Function to convert day name to day code
def day_name_to_code(day_name):
    mapping = {
        "Monday": "M",
        "Tuesday": "TU",
        "Wednesday": "W",
        "Thursday": "TH",
        "Friday": "F",
        "Saturday": "SA",
        "Sunday": "SU"
    }
    return mapping.get(day_name, day_name)

# Function to convert schedule_time from natural language to ICS-like format
def convert_to_ics_format(schedule_time):
    if schedule_time == "NO DATA" or not schedule_time:
        return "NO DATA"
    
    # Remove quotes if present
    schedule_time = schedule_time.strip('"')
    
    # Split by semicolon to get day-specific time slots
    day_schedules = [s.strip() for s in schedule_time.split(";")]
    
    # Pattern to match day and time: "DayName HH:MM-HH:MM"
    pattern = r"(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)\s+(\d{1,2}:\d{2})-(\d{1,2}:\d{2})"
    
    ics_parts = []
    
    for day_schedule in day_schedules:
        if not day_schedule:
            continue
            
        match = re.search(pattern, day_schedule)
        if match:
            day_name, start_time, end_time = match.groups()
            day_code = day_name_to_code(day_name)
            ics_parts.append(f"{day_code}/{start_time}-{end_time}")
    
    return ",".join(ics_parts)

# Read the CSV file and convert schedule_time entries
with open(input_file, 'r', encoding='utf-8') as f:
    reader = csv.reader(f, delimiter=';')
    headers = next(reader)
    rows = list(reader)

# Convert each schedule_time entry
for row in rows:
    if len(row) > 10:  # Make sure the row has a schedule_time field
        schedule_time = row[10]
        row[10] = convert_to_ics_format(schedule_time)

# Write the converted data to a new CSV file
with open(output_file, 'w', encoding='utf-8', newline='') as f:
    writer = csv.writer(f, delimiter=';')
    writer.writerow(headers)
    writer.writerows(rows)

print(f"Conversion complete! Output saved to {output_file}")
print(f"Processed {len(rows)} rows.") 