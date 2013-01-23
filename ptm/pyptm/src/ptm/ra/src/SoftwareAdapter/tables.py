import sqlalchemy
from sqlalchemy import Table, Column, Integer, String, ForeignKey, Unicode, PrimaryKeyConstraint, Boolean

md = sqlalchemy.MetaData()

package_table = Table('package', md,
	Column('id', Unicode(32), primary_key = True),
	Column('application', Unicode(255), nullable = False),
	Column('state', Integer, nullable = False, default = 0)
)
