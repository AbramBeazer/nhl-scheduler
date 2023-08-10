# nhl-scheduler

Completed:
 - Correctly creates 41 home and 41 away games for each team, with correct number of divisional, intradivisional, and intraconference matchups.
 - Games are scheduled from a specified start date for about as long as a regular NHL season, with breaks for the All-Star game and Christmas.
 - No team plays twice on one day or on three consecutive days.
 - Teams that haven't played in four days are prioritized, in order to keep one team from having a long break in between games.


To-Do:
 - Heuristics for picking the next matchup need work. Somehow, some teams will play their last game a week or more before others. I tried to remedy this by setting 16 matchups aside, scheduling every other matchup, and then, after a 4-day break, scheduling those 16 games on consecutive days, so that each team plays once in the last 2 days of the regular season a few days before the playoffs start. This only helps somewhat, as I often generate schedules where a team's last game is in the middle of April, but their second-to-last game is two weeks before that, in the end of March. Maybe I need to extend the "reserving matchups" logic to the entire scheduling process, to ensure that no team gets too many games ahead of the others.
