import math
import jaydebeapi
import psycopg2
import json
import os
import pandas as pd
import numpy as np
from datetime import datetime, timezone
from sklearn.linear_model import LogisticRegression


def compute_bt_elo(win_matrix_df, tie_matrix_df, SCALE=400, BASE=10, INIT_RATING=1000, sample_weight=None):
		ptbl_win = win_matrix_df * 2 + tie_matrix_df
		models = pd.Series(np.arange(len(ptbl_win.index)), index=ptbl_win.index)
		
		p = len(models)
		X = np.zeros([p * (p - 1) * 2, p])
		Y = np.zeros(p * (p - 1) * 2)

		cur_row = 0
		sample_weights = []
		for m_a in ptbl_win.index:
			for m_b in ptbl_win.columns:
				if m_a == m_b:
					continue
				if math.isnan(ptbl_win.loc[m_a, m_b]) or math.isnan(ptbl_win.loc[m_b, m_a]):
					continue
				X[cur_row, models[m_a]] = +math.log(BASE)
				X[cur_row, models[m_b]] = -math.log(BASE)
				Y[cur_row] = 1.0
				sample_weights.append(ptbl_win.loc[m_a, m_b])

				X[cur_row + 1, models[m_a]] = math.log(BASE)
				X[cur_row + 1, models[m_b]] = -math.log(BASE)
				Y[cur_row + 1] = 0.0
				sample_weights.append(ptbl_win.loc[m_b, m_a])
				cur_row += 2
		X = X[:cur_row]
		Y = Y[:cur_row]

		lr = LogisticRegression(fit_intercept=False, penalty=None, tol=1e-6)
		lr.fit(X, Y, sample_weight=sample_weights)
		elo_scores = SCALE * lr.coef_[0] + INIT_RATING
		# TODO: anchor to some value
		# elo_scores += 1114 - elo_scores[models["..."]]
		return pd.Series(elo_scores, index=models.index).sort_values(ascending=False)


USE_H2_DB = os.getenv('USE_H2_DB')
DB_PW = os.getenv('DB_PASSWORD_LLMAPE')
DB_HOST = os.getenv('DB_HOST')

if DB_HOST == None:
	DB_HOST = 'localhost'

conn = None

if USE_H2_DB == "true":

	h2_jar_path = "./drivers/h2.jar"
	jdbc_url = "jdbc:h2:file:../../../../target/h2db/db/llmape;DB_CLOSE_DELAY=-1"
	driver_class = "org.h2.Driver"

	conn = jaydebeapi.connect(
		driver_class,
		jdbc_url,
		["llmape", DB_PW],
		h2_jar_path
	)

else:
	conn = psycopg2.connect(
    host=DB_HOST,
    port=5432,
    database="llmape",
    user="llmape",
    password=DB_PW
)

CATEGORIES = ["", "HINT_GENERATION", "EXERCISE_GENERATION", "CODE_ASSESSMENT"]


query = f"""
	SELECT ID, MODEL_NAME FROM MODEL WHERE ACTIVE = TRUE
"""

models_df = pd.read_sql(query, conn)
models_df.columns = [col.upper() for col in models_df.columns]

all_model_ids = models_df['ID'].to_list()
all_model_names = models_df['MODEL_NAME'].to_list()

for CATEGORY in CATEGORIES:

	query = f"""
		SELECT
			winner.id AS winner_model,
			loser.id AS loser_model,
			COUNT(*) AS win_count
		FROM battle
		JOIN model AS winner ON battle.winner_model_id = winner.id
		JOIN model AS model1 ON battle.model1_id = model1.id
		JOIN model AS model2 ON battle.model2_id = model2.id
		-- Determine which of model1 or model2 is the loser
		JOIN model AS loser ON (
			(battle.winner_model_id = model1.id AND loser.id = model2.id) OR
			(battle.winner_model_id = model2.id AND loser.id = model1.id)
		)
		JOIN prompt ON battle.prompt_id = prompt.id
		WHERE prompt.category = '{CATEGORY}' OR '{CATEGORY}' = '' AND model1.active = TRUE AND model2.active = TRUE
		GROUP BY winner.id, loser.id
		ORDER BY win_count DESC;
	"""

	df = pd.read_sql(query, conn)
	df.columns = ['winner_model', 'loser_model', 'win_count']

	win_matrix_df = df.pivot_table(
		index="winner_model",  # Rows
		columns="loser_model",  # Columns
		values="win_count",
		fill_value=0  # Replace NaN with 0 (i.e., no wins)
	)

	#ensure all models appear on both axes
	win_matrix_df = win_matrix_df.reindex(index=all_model_ids, columns=all_model_ids, fill_value=0)

	query = f"""
		SELECT
			model1.id AS modelA,
			model2.id AS modelB,
			COUNT(*) AS tie_count
		FROM battle
		JOIN model AS model1 ON battle.model1_id = model1.id
		JOIN model AS model2 ON battle.model2_id = model2.id
		JOIN prompt ON battle.prompt_id = prompt.id
		WHERE
			battle.winner_model_id IS NULL
			AND battle.vote_timestamp IS NOT NULL
			AND (prompt.category = '{CATEGORY}' OR '{CATEGORY}' = '')
			AND model1.active = TRUE
			AND model2.active = TRUE
		GROUP BY model1.id, model2.id
	"""
	df_ties = pd.read_sql(query, conn)
	df_ties.columns = ['modelA', 'modelB', 'tie_count']

	tie_matrix_df = df_ties.pivot_table(
		index="modelA",  # Rows
		columns="modelB",  # Columns
		values="tie_count",
		fill_value=0  # Replace NaN with 0 (i.e., no wins)
	)

	#ensure all models appear on both axes
	tie_matrix_df = tie_matrix_df.reindex(index=all_model_ids, columns=all_model_ids, fill_value=0)
	tie_matrix_df = tie_matrix_df + tie_matrix_df.T

	elo_bt_ratings = compute_bt_elo(win_matrix_df, tie_matrix_df).round(2)

	ratings_dict = elo_bt_ratings.to_dict()

	leaderboard_list = [
		{"id": model, "score": score}
		for model, score in ratings_dict.items()
	]

	leaderboard_json_string = json.dumps(leaderboard_list)

	INSERT_CATEGORY_STRING = CATEGORY
	if CATEGORY == '': 
		INSERT_CATEGORY_STRING = "NULL" 
	else: 
		INSERT_CATEGORY_STRING = "'" + CATEGORY + "'"

	UTC_NOW_TIMESTAMP = datetime.now(timezone.utc)
	NAIVE_UTC_NOW_TIMESTAMP = UTC_NOW_TIMESTAMP.replace(tzinfo=None).isoformat()

	cursor = conn.cursor()
	cursor.execute(f"""
		INSERT INTO Leaderboard_Entry (Id, Entry_Json, Category, Timestamp)
		VALUES (nextval('sequence_generator'), '{leaderboard_json_string}', {INSERT_CATEGORY_STRING}, '{NAIVE_UTC_NOW_TIMESTAMP}');
	"""
	)
	conn.commit()
	cursor.close()



conn.close()


