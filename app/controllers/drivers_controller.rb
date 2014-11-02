class DriversController < ApplicationController
  skip_before_action :verify_authenticity_token

  def control
    data = {:success => true, :result => "SUCCESS"}
    render :json => data
  end
end
